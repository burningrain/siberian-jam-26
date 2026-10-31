package com.github.br.syberian.jam26.ecs.systems.physics;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.github.br.libgdx.common.structure.GameManager;
import com.github.br.libgdx.common.structure.SimpleUtils;
import com.github.br.syberian.jam26.ecs.components.DeleteIntentComponent;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;

public class PhysicsSystem extends BaseSystem implements EntitySubscription.SubscriptionListener {

    private final GameManager gameManager;

    private final World world;
    private static final float TIME_STEP = 1 / 60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private float accumulator = 0;

    protected PhysicsContactListener contactListener;
    protected ComponentMapper<PhysicsComponent> mPhysics;
    protected ComponentMapper<PlayerComponent> mPlayer;
    protected ComponentMapper<TransformComponent> mTransform; // Добавляем маппер трансформа

    public World getBox2dWorld() {
        return world;
    }

    public PhysicsSystem(GameManager gameManager, PhysicsContactListener contactListener) {
        this.gameManager = gameManager;
        this.contactListener = contactListener;
        this.world = new World(new Vector2(0, 0), true);
    }

    @Override
    protected void initialize() {
        Aspect.Builder physicsAspect = Aspect.all(PhysicsComponent.class);

        // Регистрируем слушатель жизненного цикла
        getWorld().getAspectSubscriptionManager()
            .get(physicsAspect)
            .addSubscriptionListener(this);

        world.setContactListener(contactListener);

        this.mPhysics = getWorld().getMapper(PhysicsComponent.class);
        this.mPlayer = getWorld().getMapper(PlayerComponent.class);
        this.mTransform = getWorld().getMapper(TransformComponent.class);
    }

    @Override
    public void inserted(IntBag entities) {
    }

    @Override
    public void removed(IntBag entities) {
        for (int i = 0; i < entities.size(); i++) {
            int entityId = entities.get(i);
            destroyPhysicsBody(entityId);
        }
    }

    @Override
    protected void processSystem() {
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += delta;
        while (accumulator >= TIME_STEP) {
            this.world.step(TIME_STEP, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
            accumulator -= TIME_STEP;
        }

        // --- ИСПРАВЛЕНИЕ: ФИЗИКА ЗАВЕРШИЛАСЬ, СИНХРОНИЗИРУЕМ ДАННЫЕ В ECS СЕТКУ ---
        syncPhysicsToTransform();

        // --- ТЕПЕРЬ БЕЗОПАСНО УДАЛЯЕМ ---
        handleDeletedEntities();
    }

    private void syncPhysicsToTransform() {
        SimpleUtils simpleUtils = gameManager.simpleUtils;

        // Достаем все живые сущности с физикой и трансформом
        IntBag entities = getWorld().getAspectSubscriptionManager()
            .get(Aspect.all(PhysicsComponent.class, TransformComponent.class))
            .getEntities();

        for (int i = 0; i < entities.size(); i++) {
            int entityId = entities.get(i);
            PhysicsComponent physics = mPhysics.get(entityId);
            TransformComponent transform = mTransform.get(entityId);

            if (physics != null && physics.body != null && transform != null) {
                // 1. Получаем позицию ЦЕНТРА тела в метрах из Box2D
                Vector2 centerMetres = physics.body.getPosition();

                // 2. Переводим координаты центра в ПИКСЕЛИ (Units)
                float centerPixelsX = simpleUtils.convertMetresToUnits(centerMetres.x);
                float centerPixelsY = simpleUtils.convertMetresToUnits(centerMetres.y);

                // 3. Достаем полу-размеры объекта в ПИКСЕЛЯХ напрямую из формы тела.
                // Это сработает автоматически для любого размера коробки/игрока/стены.
                float halfWidthPixels = 0;
                float halfHeightPixels = 0;

                if (!physics.body.getFixtureList().isEmpty()) {
                    Fixture fixture = physics.body.getFixtureList().first();
                    if (fixture.getShape() instanceof PolygonShape) {
                        PolygonShape shape = (PolygonShape) fixture.getShape();
                        Vector2 tmpVertex = new Vector2();
                        shape.getVertex(2, tmpVertex); // Извлекаем правый верхний угол в локальных метрах

                        // Переводим локальные метры формы в пиксели
                        halfWidthPixels = simpleUtils.convertMetresToUnits(tmpVertex.x);
                        halfHeightPixels = simpleUtils.convertMetresToUnits(tmpVertex.y);
                    }
                }

                // 4. ИСПРАВЛЕНИЕ: Вычитаем половину пикселей из центра, получая ЛЕВЫЙ НИЖНИЙ УГОЛ
                transform.x = centerPixelsX - halfWidthPixels;
                transform.y = centerPixelsY - halfHeightPixels;

                // Переводим угол из радиан в градусы для рендерера LibGDX
                transform.rotation = physics.body.getAngle() * MathUtils.radiansToDegrees;
            }
        }
    }

    private void handleDeletedEntities() {
        IntBag targetsToDelete = getWorld().getAspectSubscriptionManager()
            .get(Aspect.all(DeleteIntentComponent.class, PhysicsComponent.class))
            .getEntities();
        for (int i = 0; i < targetsToDelete.size(); i++) {
            int entityId = targetsToDelete.get(i);

            // 1. Сначала безопасно удаляем тело из Box2D, пока компонент еще на месте
            destroyPhysicsBody(entityId);

            // 2. И только теперь полностью удаляем саму сущность из ECS мира
            getWorld().delete(entityId);
        }
    }

    @Override
    protected void dispose() {
        this.world.dispose();
    }

    private void destroyPhysicsBody(int entityId) {
        PhysicsComponent physics = mPhysics.get(entityId);
        // Защита: если тело уже удалено вручную при рестарте, метод просто ничего не сделает
        if (physics != null && physics.body != null) {
            physics.body.setUserData(null);
            world.destroyBody(physics.body);
            physics.body = null;
        }
    }

}
