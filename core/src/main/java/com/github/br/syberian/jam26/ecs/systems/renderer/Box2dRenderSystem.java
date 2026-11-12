package com.github.br.syberian.jam26.ecs.systems.renderer;

import com.artemis.annotations.All;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.libgdx.common.structure.GameManager;
import com.github.br.libgdx.common.structure.GameSettings;
import com.github.br.libgdx.common.structure.screen.ui.CustomLayerRenderer;
import com.github.br.libgdx.common.structure.screen.ui.CustomOrthogonalTiledMapRenderer;
import com.github.br.syberian.jam26.ecs.components.RenderComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;
import com.github.br.syberian.jam26.ecs.systems.UtilsObjectsHolder;
import com.github.br.syberian.jam26.ecs.systems.physics.PhysicsSystem;

import box2dLight.RayHandler;

@All({TransformComponent.class, RenderComponent.class})
public class Box2dRenderSystem extends RenderSystem {

    private Box2DDebugRenderer debugRenderer;
    private final World box2dWorld;

    private final Matrix4 debugMatrix = new Matrix4();

    private GameManager gameManager;

    // важно, чтобы ссылка уже была. порядок инициализации такой, что ортогональный уже ждет наличие ссылки
    private final CustomLayerRenderer customLayerRenderer = new CustomLayerRenderer() {
        @Override
        public void render(MapLayer layer, Viewport viewport) {
            renderBox2dLight((OrthographicCamera) viewport.getCamera(), gameManager.gameSettings);
        }

        @Override
        public void resize(Viewport viewport, int width, int height) {
        }
    };

    public Box2dRenderSystem(World box2dWorld) {
        this.box2dWorld = box2dWorld;
    }

    public void init(CustomOrthogonalTiledMapRenderer renderer) {
        super.init(renderer);

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
        super.initialize();

        UtilsObjectsHolder utilsObjectsHolder = getWorld().getSystem(UtilsObjectsHolder.class);
        gameManager = utilsObjectsHolder.getGameManager();
    }

    public void renderBox2dLight(OrthographicCamera camera, GameSettings gameSettings) {
        float ppm = gameSettings.getPpu();

        debugMatrix.set(camera.combined).scl(ppm); // переводим из метров в пиксели
        if (gameSettings.isRenderDebugBox2d()) {
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

    public CustomLayerRenderer getCustomLayerRenderer() {
        return customLayerRenderer;
    }

}
