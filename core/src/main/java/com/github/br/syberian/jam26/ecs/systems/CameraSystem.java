package com.github.br.syberian.jam26.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;
import com.github.br.syberian.jam26.ecs.systems.renderer.Box2dRenderSystem;

@All({PlayerComponent.class})
public class CameraSystem extends IteratingSystem {

    private static final float lerp = 4.0f; // Скорость догона (чем выше, тем быстрее камера догоняет)

    private static final float ZOOM_SPEED = -5.0f; // Скорость изменения зума

    // Дефолтные настройки зума (1.0f - оригинальный размер, > 1.0f - дальше, < 1.0f - ближе)
    private float targetZoom = 1.0f;
    private static final float NORMAL_ZOOM = 1.0f;
    private static final float SCOPE_ZOOM = 1.4f; // Отдаление для обзора в стиле Hotline Miami

    protected ComponentMapper<PlayerComponent> mPlayer;
    protected ComponentMapper<TransformComponent> mTransform;

    @Override
    protected void initialize() {
        Aspect.Builder physicsAspect = Aspect.all(PhysicsComponent.class);

        // Регистрируем слушатель жизненного цикла
        getWorld().getAspectSubscriptionManager()
            .get(physicsAspect)
            .addSubscriptionListener(this);
        this.mPlayer = getWorld().getMapper(PlayerComponent.class);
        this.mTransform = getWorld().getMapper(TransformComponent.class);
    }

    @Override
    protected void process(int entityId) {
        Box2dRenderSystem system = getWorld().getSystem(Box2dRenderSystem.class);
        OrthographicCamera camera = (OrthographicCamera) system.getCamera();

        TransformComponent player = mTransform.get(entityId);

        float deltaTime = Gdx.graphics.getDeltaTime();

        camera.zoom = 4.5f; //TODO поправить позже хардкод

        camera.position.x += (player.x - camera.position.x) * lerp * deltaTime;
        camera.position.y += (player.y - camera.position.y) * lerp * deltaTime;
    }

}
