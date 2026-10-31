package com.github.br.syberian.jam26.screens;

import com.artemis.World;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.libgdx.common.structure.GameSettings;
import com.github.br.libgdx.common.structure.artemis.EmptyInjector;
import com.github.br.libgdx.common.structure.screen.AbstractTiledMapUiScreen;
import com.github.br.libgdx.common.structure.screen.ui.CustomOrthogonalTiledMapRenderer;
import com.github.br.syberian.jam26.Constants;
import com.github.br.syberian.jam26.EntityFactory;
import com.github.br.syberian.jam26.ecs.systems.PlayerInputSystem;
import com.github.br.syberian.jam26.ecs.systems.physics.PhysicsContactListener;
import com.github.br.syberian.jam26.ecs.systems.physics.PhysicsSystem;
import com.github.br.syberian.jam26.ecs.systems.renderer.RenderSystem;
import com.github.br.syberian.jam26.UserActorFactory;
import com.github.br.syberian.jam26.ui.fx.CustomVfxEffectPooledFactoryImpl;

public class TestRoomScreen extends AbstractTiledMapUiScreen<UserActorFactory, CustomVfxEffectPooledFactoryImpl> {

    private EntityFactory entityFactory;
    private com.artemis.World artemisWorld;

    public TestRoomScreen(String tiledMapPath, String pathToSkin, boolean isVfxEnabled, boolean isGlProfileActive) {
        super(tiledMapPath, pathToSkin, isVfxEnabled, isGlProfileActive);
    }

    @Override
    public void show() {
        AssetManager assetManager = getGameManager().assetManager;
        entityFactory = new EntityFactory(assetManager);
        artemisWorld = createECS(entityFactory);

        super.show();
    }

    private World createECS(EntityFactory entityFactory) {
        PhysicsContactListener contactListener = new PhysicsContactListener();
        PhysicsSystem physicsSystem = new PhysicsSystem(contactListener);
        com.artemis.WorldConfiguration config = new com.artemis.WorldConfigurationBuilder()
            .with(new TagManager())

            // Сбор ввода игрока
            .with(new PlayerInputSystem())

            // физика
            .with(contactListener)
            .with(physicsSystem)

            // ФАБРИКИ И СИСТЕМЫ УПРАВЛЕНИЯ
            .with(entityFactory)

            // Отрисовка
            .with(new RenderSystem(
                physicsSystem.getBox2dWorld()
            ).setDebugBox2d(Constants.IS_DEBUG_BOX2D))
            .build();

        // !!! ЖЕСТКОЕ ПРАВИЛО ДЛЯ ВЕБА:
        // Говорим Artemis отказаться от автоматического сканирования полей дженериков
        config.setInjector(EmptyInjector.INSTANCE);
        return new com.artemis.World(config);
    }

    @Override
    protected UserActorFactory createActionFactory(Skin gameSkin, AssetManager assetManager, GameSettings gameSettings) {
        UserActorFactory userActorFactory = new UserActorFactory(gameSkin, assetManager);
        entityFactory.setUserActorFactory(userActorFactory);
        userActorFactory.setEntityFactory(entityFactory);

        return userActorFactory;
    }

    @Override
    protected CustomVfxEffectPooledFactoryImpl createVfxEffectPoolFactory(AssetManager assetManager, GameSettings gameSettings) {
        return new CustomVfxEffectPooledFactoryImpl();
    }

    @Override
    protected void afterShow(
        TiledMap tiledMap,
        CustomOrthogonalTiledMapRenderer renderer,
        Viewport viewport,
        UserActorFactory actorFactory,
        CustomVfxEffectPooledFactoryImpl vfxEffectPoolFactory
    ) {
        RenderSystem renderSystem = artemisWorld.getSystem(RenderSystem.class);
        renderSystem.init(renderer, viewport, camera);
    }

    @Override
    protected void update(float delta, Viewport viewport) {
        //TODO InputSystem встроена в ECS - нормально или будет глючить ???

        artemisWorld.setDelta(delta);
        artemisWorld.process();
    }

    @Override
    public void dispose() {
        artemisWorld.dispose();
        super.dispose();
    }

}
