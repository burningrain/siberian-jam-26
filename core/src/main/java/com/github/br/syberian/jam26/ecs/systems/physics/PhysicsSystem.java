package com.github.br.syberian.jam26.ecs.systems.physics;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.github.br.syberian.jam26.ecs.components.DeleteIntentComponent;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;

public class PhysicsSystem extends BaseSystem implements EntitySubscription.SubscriptionListener  {

    private final World world;
    private static final float TIME_STEP = 1 / 60f;
    private static final int VELOCITY_ITERATIONS = 6;
    private static final int POSITION_ITERATIONS = 2;
    private float accumulator = 0;

    protected PhysicsContactListener contactListener;
    protected ComponentMapper<PhysicsComponent> mPhysics;
    protected ComponentMapper<PlayerComponent> mPlayer;

    public World getBox2dWorld() {
        return world;
    }

    public PhysicsSystem(PhysicsContactListener contactListener) {
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

        // --- ФИЗИКА ЗАВЕРШИЛАСЬ, ТЕПЕРЬ БЕЗОПАСНО УДАЛЯЕМ ---
        handleDeletedEntities();
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
