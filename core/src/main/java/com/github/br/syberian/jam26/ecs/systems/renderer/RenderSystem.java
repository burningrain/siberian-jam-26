package com.github.br.syberian.jam26.ecs.systems.renderer;

import com.artemis.BaseSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.libgdx.common.structure.screen.ui.CustomOrthogonalTiledMapRenderer;

public class RenderSystem extends BaseSystem {

    private Box2DDebugRenderer debugRenderer;

    private final World box2dWorld;
    private CustomOrthogonalTiledMapRenderer renderer;
    private  Viewport viewport;
    private  Camera camera;
    private boolean debugBox2d;

    public RenderSystem(World box2dWorld) {
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

        // Создаем дебажный рендерер при инициализации системы
        this.debugRenderer = new Box2DDebugRenderer();

        // Настраиваем дебажные флаги (можно отключать ненужное)
        this.debugRenderer.setDrawBodies(true);   // Контуры тел
        this.debugRenderer.setDrawJoints(true);   // Суставы (отлично покажет рельсы призматических джоинтов)
        this.debugRenderer.setDrawAABBs(false);   // Хитбоксы оптимизации (обычно не нужны)
        this.debugRenderer.setDrawInactiveBodies(false);
        this.debugRenderer.setDrawVelocities(false);
    }

    @Override
    protected void initialize() {
        //this.renderMapper = getWorld().getMapper(RenderComponent.class);
    }

    @Override
    protected void processSystem() {
        if (debugBox2d) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

            viewport.apply();
            camera.update();
            debugRenderer.render(box2dWorld, camera.combined);
            return;
        }

        renderer.render();
    }

    public CustomOrthogonalTiledMapRenderer getRenderer() {
        return renderer;
    }

    public BaseSystem setDebugBox2d(boolean debugBox2d) {
        this.debugBox2d = debugBox2d;
        return this;
    }

    public boolean isDebugBox2d() {
        return debugBox2d;
    }
}
