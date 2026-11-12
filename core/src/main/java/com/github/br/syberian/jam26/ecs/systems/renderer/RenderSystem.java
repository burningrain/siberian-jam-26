package com.github.br.syberian.jam26.ecs.systems.renderer;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.utils.IntBag;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.libgdx.common.structure.GameSettings;
import com.github.br.libgdx.common.structure.screen.ui.CustomOrthogonalTiledMapRenderer;
import com.github.br.syberian.jam26.ecs.components.HudComponent;
import com.github.br.syberian.jam26.ecs.components.RenderComponent;
import com.github.br.syberian.jam26.ecs.components.TransformComponent;
import com.github.br.syberian.jam26.ecs.systems.UtilsObjectsHolder;

public abstract class RenderSystem extends BaseSystem implements EntitySubscription.SubscriptionListener {

    private CustomOrthogonalTiledMapRenderer renderer;
    private GLProfiler profiler;

    protected ComponentMapper<TransformComponent> mTransform;
    protected ComponentMapper<RenderComponent> mRender;

    protected ComponentMapper<HudComponent> mHud;

    public void init(CustomOrthogonalTiledMapRenderer renderer) {
        this.renderer = renderer;
        this.profiler = new GLProfiler(Gdx.graphics);
    }

    protected CustomOrthogonalTiledMapRenderer getRenderer() {
        return renderer;
    }

    @Override
    protected void initialize() {
        this.mTransform = getWorld().getMapper(TransformComponent.class);
        this.mRender = getWorld().getMapper(RenderComponent.class);
        this.mHud = getWorld().getMapper(HudComponent.class);

        getWorld().getAspectSubscriptionManager()
            .get(Aspect.all(TransformComponent.class, RenderComponent.class))
            .addSubscriptionListener(this);
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
            .get(Aspect.all(TransformComponent.class, RenderComponent.class))
            .getEntities();
        for (int i = 0; i < entities.size(); i++) {
            int entityId = entities.get(i);
            process(entityId);
        }
    }

    protected void process(int entityId) {
        if (mHud.has(entityId)) {
            // для HUD не выравниваются UI-акторы
            return;
        }

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

        // Точка вращения (Origin) в box2d по центру, поэтому и у актора так ставим
        actor.setOrigin(width / 2f, height / 2f);

        actor.setPosition(
            transform.x,
            transform.y
        );
        actor.setRotation(transform.rotationDegree);
    }

    @Override
    public void inserted(IntBag entities) {
        for (int i = 0; i < entities.size(); i++) {
            int entityId = entities.get(i);
            RenderComponent render = mRender.get(entityId);

            // Если у сущности есть Actor и указано имя слоя, автоматически добавляем его на сцену карты
            if (render != null && render.actor != null && render.layerName != null) {
                getRenderer().addToStage(render.layerName, render.actor);
            }
        }
    }

    @Override
    public void removed(IntBag entities) {
        for (int i = 0; i < entities.size(); i++) {
            int entityId = entities.get(i);
            RenderComponent render = mRender.get(entityId);

            // Заодно закрываем ваш старый FIXME: автоматически удаляем Actor со сцены при уничтожении сущности!
            if (render != null && render.actor != null) {
                render.actor.remove();
            }
        }
    }

}
