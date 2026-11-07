package com.github.br.syberian.jam26.ecs.systems.renderer;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.libgdx.common.structure.GameSettings;
import com.github.br.libgdx.common.structure.screen.ui.CustomOrthogonalTiledMapRenderer;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;
import com.github.br.syberian.jam26.ecs.systems.UtilsObjectsHolder;

public abstract class RenderSystem extends BaseSystem {

    private CustomOrthogonalTiledMapRenderer renderer;
    private GLProfiler profiler;

    public void init(CustomOrthogonalTiledMapRenderer renderer) {
        this.renderer = renderer;

        this.profiler = new GLProfiler(Gdx.graphics);
    }

    @Override
    protected void processSystem() {
        updateEntities();

        UtilsObjectsHolder utilsObjectsHolder = getWorld().getSystem(UtilsObjectsHolder.class);
        GameSettings gameSettings = utilsObjectsHolder.getGameManager().gameSettings;
        Viewport viewport = utilsObjectsHolder.getViewport();
        OrthographicCamera camera = (OrthographicCamera) viewport.getCamera();

        if (gameSettings.isActiveGlProfiler()) {
            profiler.enable();
        }

        render(viewport, camera, gameSettings);

        if (gameSettings.isActiveGlProfiler()) {
            String simpleName = this.getClass().getSimpleName();
            Gdx.app.log(simpleName, "Draw Calls: " + profiler.getDrawCalls());
            Gdx.app.log(simpleName, "Texture Bindings: " + profiler.getTextureBindings());
            Gdx.app.log(simpleName, "Shader Switches: " + profiler.getShaderSwitches());
            Gdx.app.log(simpleName, "Vertices: " + profiler.getVertexCount().total);
            Gdx.app.log(simpleName, "\n");

            profiler.reset();
        }
    }

    protected void render(Viewport viewport, OrthographicCamera camera, GameSettings gameSettings) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!gameSettings.isActiveTiledRender()) {
            return;
        }

        if (gameSettings.isCenterCamera()) {
            viewport.apply(true);
        } else {
            viewport.apply(false);
        }

        camera.position.x = Math.round(camera.position.x);
        camera.position.y = Math.round(camera.position.y);
        camera.update();
        renderer.getBatch().setProjectionMatrix(camera.combined);

        renderer.setView(camera);
        renderer.render();
    }

    protected void updateEntities() {
        IntBag entities = getWorld().getAspectSubscriptionManager()
            .get(Aspect.all(PhysicsComponent.class, TransformComponent.class))
            .getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int entityId = entities.get(i);
            process(entityId);
        }
    }

    protected abstract void process(int entityId);

}
