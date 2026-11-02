package com.github.br.syberian.jam26.screens;

import com.artemis.World;
import com.artemis.managers.TagManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.libgdx.common.structure.GameManager;
import com.github.br.libgdx.common.structure.GameSettings;
import com.github.br.libgdx.common.structure.artemis.EmptyInjector;
import com.github.br.libgdx.common.structure.screen.AbstractTiledMapUiScreen;
import com.github.br.libgdx.common.structure.screen.RestartScreen;
import com.github.br.libgdx.common.structure.screen.statemachine.GameScreenState;
import com.github.br.libgdx.common.structure.screen.ui.CustomOrthogonalTiledMapRenderer;
import com.github.br.syberian.jam26.EntityFactory;
import com.github.br.syberian.jam26.IUserActorFactory;
import com.github.br.syberian.jam26.Resources;
import com.github.br.syberian.jam26.TriggerActionFactory;
import com.github.br.syberian.jam26.UserActorFactory;
import com.github.br.syberian.jam26.ecs.systems.CameraSystem;
import com.github.br.syberian.jam26.ecs.systems.LevelControlSystem;
import com.github.br.syberian.jam26.ecs.systems.PlayerInputSystem;
import com.github.br.syberian.jam26.ecs.systems.SecurityCameraSystem;
import com.github.br.syberian.jam26.ecs.systems.TriggerSystem;
import com.github.br.syberian.jam26.ecs.systems.UtilsObjectsHolder;
import com.github.br.syberian.jam26.ecs.systems.physics.PhysicsContactListener;
import com.github.br.syberian.jam26.ecs.systems.physics.PhysicsSystem;
import com.github.br.syberian.jam26.ecs.systems.renderer.Box2dRenderSystem;
import com.github.br.syberian.jam26.ui.fx.CustomVfxEffectPooledFactoryImpl;

public class PhysicsLevelScreen extends AbstractTiledMapUiScreen<IUserActorFactory, CustomVfxEffectPooledFactoryImpl>
    implements RestartScreen {

    private EntityFactory entityFactory;
    private com.artemis.World artemisWorld;

    private Music music;

    private GameScreenState nextState;

    public PhysicsLevelScreen(
        GameScreenState nextState,
        String tiledMapPath,
        String pathToSkin,
        boolean isVfxEnabled,
        boolean isGlProfileActive,
        boolean isTiledRenderActive
    ) {
        super(tiledMapPath, pathToSkin, isVfxEnabled, isGlProfileActive, isTiledRenderActive);
        this.nextState = nextState;
    }

    @Override
    public void restart() {
        getGameManager().screenStateManager.changeCurrentState(GameScreens.PHYSICS_LEVEL_1_SCREEN);
    }

    @Override
    public void show() {
        GameManager gameManager = getGameManager();
        entityFactory = new EntityFactory(gameManager);
        artemisWorld = createECS(entityFactory);

        super.show();
        Gdx.app.log("DEBUG", "Screen [" + this.getClass().getSimpleName() + " is loaded");
    }

    @Override
    public void triggerExit() {
        getGameManager().screenStateManager.changeCurrentState(nextState);
    }

    private World createECS(EntityFactory entityFactory) {
        PhysicsContactListener contactListener = new PhysicsContactListener();
        PhysicsSystem physicsSystem = new PhysicsSystem(getGameManager(), contactListener);
        com.artemis.WorldConfiguration config = new com.artemis.WorldConfigurationBuilder()
            .with(new TagManager())

            // Сбор ввода игрока
            .with(new PlayerInputSystem(getGameManager()))

            // физика
            .with(contactListener)
            .with(physicsSystem)
            .with(new TriggerSystem())
            .with(new TriggerActionFactory(this))

            // бизнес-логика игры
            .with(new SecurityCameraSystem(getGameManager()))
            .with(new LevelControlSystem())

            // Отрисовка
            .with(new CameraSystem())
            .with(new Box2dRenderSystem(
                physicsSystem.getBox2dWorld(),
                getGameManager()
            ))

            // ФАБРИКИ И СИСТЕМЫ УПРАВЛЕНИЯ
            .with(entityFactory)
            .with(new UtilsObjectsHolder())
            .build();

        // !!! ЖЕСТКОЕ ПРАВИЛО ДЛЯ ВЕБА:
        // Говорим Artemis отказаться от автоматического сканирования полей дженериков
        config.setInjector(EmptyInjector.INSTANCE);
        return new com.artemis.World(config);
    }

    @Override
    protected IUserActorFactory createActionFactory(Skin gameSkin, AssetManager assetManager, GameSettings gameSettings) {
        UserActorFactory userActorFactory = new UserActorFactory(
            gameSkin,
            assetManager,
            Resources.Atlases.GAME_OBJECTS
        );
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
        IUserActorFactory actorFactory,
        CustomVfxEffectPooledFactoryImpl vfxEffectPoolFactory
    ) {
        Box2dRenderSystem box2dRenderSystem = artemisWorld.getSystem(Box2dRenderSystem.class);
        box2dRenderSystem.init(renderer);

        UtilsObjectsHolder objectsHolder = artemisWorld.getSystem(UtilsObjectsHolder.class);
        objectsHolder.setViewport(viewport);

        music = getGameManager().audioAssetManager.getMusic(Resources.Music.LEVEL_THEME);
        this.music.setLooping(true);
        this.music.play();
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        // FIXME кривота... нужно как-то засунуть рендеринг внутрь artemisOdb, иначе еще где вылезет что
        LevelControlSystem levelControlSystem = artemisWorld.getSystem(LevelControlSystem.class);
        if (levelControlSystem.isGameOver()) {
            return;
        }

        // рендеринг физики света после слоев tiled
        //TODO будут проблемы с шейдерами из-за того, что свет им не подчиняется?
        Box2dRenderSystem box2dRenderSystem = artemisWorld.getSystem(Box2dRenderSystem.class);
        box2dRenderSystem.renderBox2dLight(viewport);
    }

    @Override
    protected void update(float delta, Viewport viewport) {
        //TODO InputSystem встроена в ECS - нормально или будет глючить ???

        artemisWorld.setDelta(delta);
        artemisWorld.process();
    }

    @Override
    public void dispose() {
        try {
            artemisWorld.dispose();
            super.dispose();
        } catch (Exception e) {
            Gdx.app.debug(e.getMessage(), e.toString());
        }
    }

}
