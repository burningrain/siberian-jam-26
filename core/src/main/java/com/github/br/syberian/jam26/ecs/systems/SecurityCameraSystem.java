package com.github.br.syberian.jam26.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.github.br.libgdx.common.structure.GameManager;
import com.github.br.libgdx.common.structure.SimpleUtils;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;
import com.github.br.syberian.jam26.ecs.components.SecurityCameraComponent;
import com.github.br.syberian.jam26.ecs.systems.physics.PhysicsSystem;

public class SecurityCameraSystem extends IteratingSystem {

    private GameManager gameManager;

    protected ComponentMapper<SecurityCameraComponent> mCamera;
    protected ComponentMapper<PhysicsComponent> mPhysics;

    private EntitySubscription playerSubscription;
    private PhysicsSystem physicsSystem;

    // Оптимизация GC: один переиспользуемый инстанс коллбэка для RayCast на всю систему
    private final RayCastCallback visionCallback = new RayCastCallback() {
        @Override
        public float reportRayFixture(com.badlogic.gdx.physics.box2d.Fixture fixture, Vector2 point, Vector2 normal, float fraction) {
            SecurityCameraComponent cam = mCamera.get(currentCameraEntityId);
            if (fraction < cam.closestFraction) {
                cam.closestFraction = fraction;
                cam.sightedBody = fixture.getBody();
            }
            return 1f; // Возвращаем 1, чтобы продолжить сканирование до конца отрезка
        }
    };

    private int currentCameraEntityId; // Буфер для передачи ID в анонимный callback

    public SecurityCameraSystem(GameManager gameManager) {
        super(Aspect.all(SecurityCameraComponent.class, PhysicsComponent.class));
        this.gameManager = gameManager;
    }

    @Override
    protected void initialize() {
        mCamera = getWorld().getMapper(SecurityCameraComponent.class);;
        mPhysics = getWorld().getMapper(PhysicsComponent.class);;

        physicsSystem = world.getSystem(PhysicsSystem.class);
        // Автоматически подписываемся на сущность игрока
        playerSubscription = world.getAspectSubscriptionManager()
            .get(Aspect.all(PlayerComponent.class, PhysicsComponent.class));
    }

    @Override
    protected void process(int cameraEntityId) {
        IntBag players = playerSubscription.getEntities();
        if (players.isEmpty()) return;
        int playerEntityId = players.get(0); // Забираем ID главного героя

        SecurityCameraComponent cam = mCamera.get(cameraEntityId);
        PhysicsComponent camPhys = mPhysics.get(cameraEntityId);
        PhysicsComponent playerPhys = mPhysics.get(playerEntityId);

        if (camPhys.body == null || playerPhys.body == null) return;

        Vector2 cameraPos = camPhys.body.getPosition();
        Vector2 playerPos = playerPhys.body.getPosition();
        float delta = world.getDelta();

        // --- 1. АВТОМАТИЧЕСКИЙ ПОВОРОТ КАМЕРЫ (Патрулирование) ---
        cam.cameraRotationTime += delta;
        // Качается по синусоиде относительно базового направления
        cam.viewDirectionDegrees = cam.baseDirectionDegrees + MathUtils.sin(cam.cameraRotationTime * cam.patrolSpeed) * cam.patrolArc;

        // Синхронизируем поворот физического Box2D-тела (градусы -> радианы)
        camPhys.body.setTransform(cameraPos, cam.viewDirectionDegrees * MathUtils.degreesToRadians);

        // Передаем направление в объект света box2dLight для корректного рендера конуса
        if (cam.cameraLight != null) {
            cam.cameraLight.setDirection(cam.viewDirectionDegrees);
        }

        // --- 2. ПРОВЕРКА ВИДИМОСТИ (STEALTH LOGIC ИЗ GAME SCREEN) ---
        cam.isPlayerSpotted = false;

        // Проверка 1: Дистанция до игрока (с запасом 0.5м для сглаживания краев)
        float distanceToPlayer = cameraPos.dst(playerPos);
        if (distanceToPlayer > (cam.cameraRange + 0.5f)) {
            resetLight(cam);
            return;
        }

        // Проверка 2: Вычисляем угол от камеры к игроку (0...360)
        cam.toPlayer.set(playerPos).sub(cameraPos);
        float angleToPlayer = cam.toPlayer.angleDeg();

        // Проверка 3: Нормализуем текущее направление камеры в диапазон [0; 360)
        float normalizedCameraAngle = (cam.viewDirectionDegrees % 360 + 360) % 360;

        // Проверка 4: Вычисляем кратчайшую разницу углов
        float angleDiff = angleToPlayer - normalizedCameraAngle;
        while (angleDiff < -180f) angleDiff += 360f;
        while (angleDiff > 180f)  angleDiff -= 360f;
        angleDiff = Math.abs(angleDiff);

        // Компенсируем мягкие тени на краях визуального конуса (Soft Shadows)
        float softConePadding = 5f;

        // Если игрок попал в сектор обзора
        if (angleDiff <= (cam.fovDegrees + softConePadding)) {

            // Ограничиваем траекторию физического луча длиной обзора камеры
            cam.rayTarget.set(playerPos);
            if (distanceToPlayer > cam.cameraRange) {
                float fractionOfDistance = cam.cameraRange / distanceToPlayer;
                cam.rayTarget.set(cameraPos).lerp(playerPos, fractionOfDistance);
            }

            cam.closestFraction = 1f;
            cam.sightedBody = null;
            this.currentCameraEntityId = cameraEntityId; // Запоминаем текущую камеру для callback

            // Бросаем физический RayCast в мире Box2D (стены и шкафы перекроют луч)
            physicsSystem.getBox2dWorld().rayCast(visionCallback, cameraPos, cam.rayTarget);

            // Если луч беспрепятственно дошел именно до тела игрока
            if (cam.sightedBody == playerPhys.body) {
                cam.isPlayerSpotted = true;
            }
        }

        // --- 3. РЕАКЦИЯ СВЕТА НА ОБНАРУЖЕНИЕ ---
        if (cam.cameraLight != null) {
            SimpleUtils simpleUtils = gameManager.simpleUtils;
            cam.cameraLight.setPosition(
                simpleUtils.convertMetresToUnits(cameraPos.x),
                simpleUtils.convertMetresToUnits(cameraPos.y)
            );
            if (cam.isPlayerSpotted) {
                cam.cameraLight.setColor(Color.RED);

                // ЗДЕСЬ ДЕЙСТВИЕ ОБНАРУЖЕНИЯ:
                // Например, вызываем: gameManager.onPlayerSpotted(); или поднимаем глобальную тревогу.
            } else {
                cam.cameraLight.setColor(Color.YELLOW);
            }
        }
    }

    private void resetLight(SecurityCameraComponent cam) {
        if (cam.cameraLight != null) {
            cam.cameraLight.setColor(Color.YELLOW);
        }
    }

}
