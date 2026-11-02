package com.github.br.syberian.jam26.ecs.systems.renderer;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.libgdx.common.structure.GameManager;
import com.github.br.libgdx.common.structure.screen.ui.CustomOrthogonalTiledMapRenderer;
import com.github.br.syberian.jam26.Constants;
import com.github.br.syberian.jam26.ecs.components.RenderComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;
import com.github.br.syberian.jam26.ecs.systems.physics.PhysicsSystem;

import box2dLight.RayHandler;

@All({TransformComponent.class})
public class Box2dRenderSystem extends IteratingSystem {

    private final GameManager gameManager;

    private Box2DDebugRenderer debugRenderer;
    private final World box2dWorld;
    private CustomOrthogonalTiledMapRenderer renderer;

    // Выделяем память под матрицу один раз, чтобы не мусорить в куче (GC) каждый кадр
    private final Matrix4 debugMatrix = new Matrix4();

    protected ComponentMapper<TransformComponent> mTransform;
    protected ComponentMapper<RenderComponent> mRender;

    public Box2dRenderSystem(World box2dWorld, GameManager gameManager) {
        this.gameManager = gameManager;
        this.box2dWorld = box2dWorld;
    }

    public void init(CustomOrthogonalTiledMapRenderer renderer) {
        this.renderer = renderer;

        this.debugRenderer = new Box2DDebugRenderer();
        this.debugRenderer.setDrawBodies(true);
        this.debugRenderer.setDrawJoints(true);
        this.debugRenderer.setDrawAABBs(true);
        this.debugRenderer.setDrawInactiveBodies(true);
        this.debugRenderer.setDrawVelocities(true);

        // --- КАСТОМИЗАЦИЯ ЦВЕТОВ ДЕБАГА ---
        debugRenderer.SHAPE_STATIC.set(1.0f, 0.0f, 0.0f, 1.0f);
        debugRenderer.SHAPE_AWAKE.set(1.0f, 1.0f, 0.0f, 1.0f);
        debugRenderer.JOINT_COLOR.set(1.0f, 1.0f, 1.0f, 1.0f);
        debugRenderer.SHAPE_NOT_AWAKE.set(0.0f, 1.0f, 1.0f, 1.0f); // Голубой цвет для сенсоров/сонных тел
    }

    @Override
    protected void initialize() {
        this.mTransform = getWorld().getMapper(TransformComponent.class);
        this.mRender = getWorld().getMapper(RenderComponent.class);
    }

    @Override
    protected void process(int entityId) {
        TransformComponent transform = mTransform.get(entityId);
        RenderComponent render = mRender.get(entityId);

        if (render == null) {
            return;
        }

        Actor actor = render.actor;
        if (actor == null) {
            return;
        }

        float width = actor.getWidth();
        float height = actor.getHeight();

        // Точку вращения (Origin) обязательно оставляем по центру
        actor.setOrigin(width / 2f, height / 2f);

        // ИСПРАВЛЕНИЕ 1: Передаем координаты напрямую, так как мы вернули
        // Transform к пикселям левого нижнего угла в PhysicsSystem
        actor.setPosition(
            transform.x,
            transform.y
        );

        // Передаем угол вращения из физики (в градусах)
        actor.setRotation(transform.rotation);
    }

    public void renderBox2dLight(Viewport viewport) {
        float ppm = gameManager.gameSettings.getPpu();

        debugMatrix.set(viewport.getCamera().combined).scl(ppm); // потому что 1 метр - это 128
        if (Constants.IS_DEBUG_BOX2D) {
            debugRenderer.render(box2dWorld, debugMatrix);
        }

        RayHandler rayHandler = world.getSystem(PhysicsSystem.class).getRayHandler();
        rayHandler.setCombinedMatrix(debugMatrix);
        rayHandler.updateAndRender();
    }

    @Override
    protected void dispose() {
        debugRenderer.dispose();
    }

    public CustomOrthogonalTiledMapRenderer getRenderer() {
        return renderer;
    }

}
