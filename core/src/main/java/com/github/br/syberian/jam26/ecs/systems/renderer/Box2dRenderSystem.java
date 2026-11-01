package com.github.br.syberian.jam26.ecs.systems.renderer;

import com.artemis.ComponentMapper;
import com.artemis.annotations.All;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4; // Импортируем Matrix4 для масштабирования
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

@All({TransformComponent.class, RenderComponent.class})
public class Box2dRenderSystem extends IteratingSystem {

    private final GameManager gameManager;

    private Box2DDebugRenderer debugRenderer;
    private final World box2dWorld;
    private CustomOrthogonalTiledMapRenderer renderer;
    private Viewport viewport;
    private Camera camera;

    // Выделяем память под матрицу один раз, чтобы не мусорить в куче (GC) каждый кадр
    private final Matrix4 debugMatrix = new Matrix4();

    protected ComponentMapper<TransformComponent> mTransform;
    protected ComponentMapper<RenderComponent> mRender;

    public Box2dRenderSystem(World box2dWorld, GameManager gameManager) {
        this.gameManager = gameManager;
        this.box2dWorld = box2dWorld;
    }

    public void init(
        CustomOrthogonalTiledMapRenderer renderer,
        Viewport viewport,
        Camera camera
    ) {
        this.renderer = renderer;
        this.viewport = viewport;
        this.camera = camera;

        this.debugRenderer = new Box2DDebugRenderer();
        this.debugRenderer.setDrawBodies(true);
        this.debugRenderer.setDrawJoints(true);
        this.debugRenderer.setDrawAABBs(false);
        this.debugRenderer.setDrawInactiveBodies(false);
        this.debugRenderer.setDrawVelocities(false);

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

        Actor actor = render.actor;
        if (actor != null) {
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
    }

    public void renderBox2d() {
        if (Constants.IS_DEBUG_BOX2D) {
            // ИСПРАВЛЕНИЕ 2: Масштабируем матрицу камеры из метров в пиксели (Units) вашего экрана.
            // Берем текущую матрицу пиксельной камеры
            debugMatrix.set(camera.combined);

            // Умножаем матрицу на коэффициент PPM, чтобы дебаг-линии увеличились до размеров спрайтов
            float ppm = gameManager.gameSettings.getUnitsPerMeter();
            debugMatrix.scl(ppm);

            // Рендерим дебаг Box2D по новой масштабированной матрице
            debugRenderer.render(box2dWorld, debugMatrix);
        }
    }

    public void renderLightsOnly() {
        RayHandler rayHandler = world.getSystem(PhysicsSystem.class).getRayHandler();
        rayHandler.setCombinedMatrix(camera.combined); // Камера из AbstractTiledMapUiScreen
        rayHandler.updateAndRender();
    }

    @Override
    protected void dispose() {
        if (debugRenderer != null) {
            debugRenderer.dispose();
        }
    }

    public CustomOrthogonalTiledMapRenderer getRenderer() {
        return renderer;
    }

    public Viewport getViewport() {
        return viewport;
    }

    public Camera getCamera() {
        return camera;
    }

}
