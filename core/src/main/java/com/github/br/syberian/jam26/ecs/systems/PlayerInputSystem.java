package com.github.br.syberian.jam26.ecs.systems;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.github.br.libgdx.common.structure.GameManager;
import com.github.br.libgdx.common.structure.SimpleUtils;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;
import com.github.br.syberian.jam26.ecs.systems.renderer.Box2dRenderSystem;

@All({PlayerComponent.class, PhysicsComponent.class, TransformComponent.class})
public class PlayerInputSystem extends IteratingSystem {

    private final GameManager gameManager;

    // Настройка скорости игрока в МЕТРАХ в секунду (Hotline Miami — быстрый шутер, 6-8 м/с — отличная скорость)
    private static final float MOVE_SPEED_METRES = 5.0f;

    // Временные векторы, чтобы не создавать новые объекты в памяти каждый кадр (боремся с мусором для Android/Desktop)
    private final Vector2 moveDirection = new Vector2();
    private final Vector3 mouseWorldPos = new Vector3();
    private final Vector2 playerCenterPixels = new Vector2();

    protected ComponentMapper<PhysicsComponent> mPhysics;
    protected ComponentMapper<TransformComponent> mTransform;

    public PlayerInputSystem(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    protected void initialize() {
        this.mPhysics = getWorld().getMapper(PhysicsComponent.class);
        this.mTransform = getWorld().getMapper(TransformComponent.class);
    }

    @Override
    protected void process(int entityId) {
        Body body = mPhysics.get(entityId).body;
        if (body == null) return;

        TransformComponent transform = mTransform.get(entityId);
        SimpleUtils simpleUtils = gameManager.simpleUtils;

        // ==========================================
        // 1. ПЕРЕМЕЩЕНИЕ ИГРОКА (WASD / СТРЕЛОЧКИ)
        // ==========================================
        moveDirection.set(0, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            moveDirection.y += 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            moveDirection.y -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveDirection.x -= 1;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveDirection.x += 1;
        }

        // Если нажата хоть одна клавиша движения
        if (!moveDirection.isZero()) {
            // Нормализуем вектор направления, чтобы скорость по диагонали не была быстрее, чем по прямой
            moveDirection.nor();
            // Умножаем направление на скорость в метрах
            moveDirection.scl(MOVE_SPEED_METRES);
        }

        // Задаем скорость напрямую телу Box2D.
        // Если кнопки не нажаты — moveDirection равен (0,0), и игрок остановится мгновенно!
        body.setLinearVelocity(moveDirection);


        // ==========================================
        // 2. ПОВОРОТ ГЕРОЯ ЗА МЫШКОЙ (LOOK AT MOUSE)
        // ==========================================

        // Получаем камеру из системы рендеринга для перевода координат
        Box2dRenderSystem renderSystem = getWorld().getSystem(Box2dRenderSystem.class);
        Camera camera = renderSystem.getCamera();

        if (camera != null) {
            // Считываем позицию мыши на экране
            mouseWorldPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            // Переводим экранные пиксели мыши в мировые ПИКСЕЛИ (Units) нашей карты
            camera.unproject(mouseWorldPos);

            // Так как в твоем TransformComponent хранятся пиксели ЛЕВОГО НИЖНЕГО УГЛА,
            // для точного расчета угла нам нужно найти физический ЦЕНТР игрока в пикселях.
            // Берем координаты тела в метрах и переводим их в пиксели:
            playerCenterPixels.x = simpleUtils.convertMetresToUnits(body.getPosition().x);
            playerCenterPixels.y = simpleUtils.convertMetresToUnits(body.getPosition().y);

            // Находим вектор направления от центра игрока до курсора мыши в пикселях
            float deltaX = mouseWorldPos.x - playerCenterPixels.x;
            float deltaY = mouseWorldPos.y - playerCenterPixels.y;

            // Считаем угол поворота в радианах через арктангенс
            float angleRad = MathUtils.atan2(deltaY, deltaX);
            angleRad += 90 * MathUtils.degreesToRadians; // FIXME грязь, но иначе боком герой повернут

            // Поворачиваем тело в Box2D. Позицию оставляем текущую, меняем только угол симуляции
            body.setTransform(body.getPosition(), angleRad);
        }
    }
}
