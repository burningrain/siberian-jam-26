package com.github.br.syberian.jam26.screens;

import com.artemis.World;
import com.artemis.WorldConfiguration;
import com.artemis.WorldConfigurationBuilder;
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
import com.github.br.libgdx.common.structure.screen.AbstractTiledMapScreen;
import com.github.br.libgdx.common.structure.screen.RestartScreen;
import com.github.br.libgdx.common.structure.screen.statemachine.GameScreenState;
import com.github.br.libgdx.common.structure.screen.ui.CustomLayerRenderer;
import com.github.br.libgdx.common.structure.screen.ui.CustomOrthogonalTiledMapRenderer;
import com.github.br.syberian.jam26.EntityFactory;
import com.github.br.syberian.jam26.IUserActorFactory;
import com.github.br.syberian.jam26.Mappers;
import com.github.br.syberian.jam26.Resources;
import com.github.br.syberian.jam26.TriggerActionFactory;
import com.github.br.syberian.jam26.UserActorFactory;
import com.github.br.syberian.jam26.ecs.systems.CameraSystem;
import com.github.br.syberian.jam26.ecs.systems.FollowTheTargetSystem;
import com.github.br.syberian.jam26.ecs.systems.LevelControlSystem;
import com.github.br.syberian.jam26.ecs.systems.PlayerInputSystem;
import com.github.br.syberian.jam26.ecs.systems.SecurityCameraSystem;
import com.github.br.syberian.jam26.ecs.systems.TriggerSystem;
import com.github.br.syberian.jam26.ecs.systems.UtilsObjectsHolder;
import com.github.br.syberian.jam26.ecs.systems.cutscene.CutsceneSystem;
import com.github.br.syberian.jam26.ecs.systems.physics.PhysicsContactListener;
import com.github.br.syberian.jam26.ecs.systems.physics.PhysicsSystem;
import com.github.br.syberian.jam26.ecs.systems.renderer.Box2dRenderSystem;
import com.github.br.syberian.jam26.ui.fx.CustomVfxEffectPooledFactoryImpl;

public class PhysicsLevelScreen extends AbstractTiledMapScreen<IUserActorFactory>
    implements RestartScreen {

    private EntityFactory entityFactory;
    private com.artemis.World artemisWorld;

    private String levelLoopMusic;
    private Music music;

    private GameScreenState nextState;

    public PhysicsLevelScreen(
        String levelLoopMusic,
        GameScreenState nextState,
        String tiledMapPath,
        String pathToSkin
    ) {
        super(tiledMapPath, pathToSkin);
        this.levelLoopMusic = levelLoopMusic;
        this.nextState = nextState;
    }

    @Override
    public void show() {
        GameManager gameManager = getGameManager();
        entityFactory = new EntityFactory();
        artemisWorld = createECS(entityFactory);

        super.show();
        Gdx.app.log("DEBUG", "Screen [" + this.getClass().getSimpleName() + " is loaded");
    }

    @Override
    protected CustomLayerRenderer createCustomLayerRenderer() {
        Box2dRenderSystem box2dRenderSystem = artemisWorld.getSystem(Box2dRenderSystem.class);
        return box2dRenderSystem.getCustomLayerRenderer();
    }

    @Override
    public void triggerExit() {
        getGameManager().screenStateManager.changeCurrentState(nextState);
    }

    private World createECS(EntityFactory entityFactory) {
        UtilsObjectsHolder utilsObjectsHolder = new UtilsObjectsHolder();
        utilsObjectsHolder.setGameManager(getGameManager());

        PhysicsContactListener contactListener = new PhysicsContactListener();
        PhysicsSystem physicsSystem = new PhysicsSystem(contactListener);
        WorldConfiguration config = new WorldConfigurationBuilder()
            .with(new TagManager())

            .with(new CutsceneSystem())
            // Сбор ввода игрока
            .with(new PlayerInputSystem(getGameManager()))

            // физика
            .with(contactListener)
            .with(physicsSystem)
            // бизнес-логика игры
            .with(new FollowTheTargetSystem())
            .with(new SecurityCameraSystem(getGameManager()))
            .with(new LevelControlSystem())

            // Отрисовка
            .with(new CameraSystem())
            .with(new Box2dRenderSystem(physicsSystem.getBox2dWorld()))

            // ФАБРИКИ И СИСТЕМЫ УПРАВЛЕНИЯ
            .with(entityFactory)
            .with(new Mappers())
            .with(utilsObjectsHolder)
            .with(new TriggerActionFactory(this))

            .with(new TriggerSystem()) // должна быть самой последней, ибо рестартует уровень!
            .build();

        // !!! ЖЕСТКОЕ ПРАВИЛО ДЛЯ ВЕБА:
        // Говорим Artemis отказаться от автоматического сканирования полей дженериков
        config.setInjector(EmptyInjector.INSTANCE);
        return new World(config);
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
    protected void afterShow(
        TiledMap tiledMap,
        CustomOrthogonalTiledMapRenderer renderer,
        Viewport viewport,
        IUserActorFactory actorFactory
    ) {
        Box2dRenderSystem box2dRenderSystem = artemisWorld.getSystem(Box2dRenderSystem.class);
        box2dRenderSystem.init(renderer);

        UtilsObjectsHolder objectsHolder = artemisWorld.getSystem(UtilsObjectsHolder.class);
        objectsHolder.setViewport(viewport);

        playLoopMusic();
    }

    protected void playLoopMusic() {
        music = getGameManager().audioAssetManager.getMusic(levelLoopMusic);
        this.music.setLooping(true);
        this.music.play();
    }

    @Override
    public void render(float delta) {
        artemisWorld.setDelta(delta);
        artemisWorld.process();

        // FIXME кривота... нужно как-то засунуть рендеринг внутрь artemisOdb, иначе еще где вылезет что
        LevelControlSystem levelControlSystem = artemisWorld.getSystem(LevelControlSystem.class);
        if (levelControlSystem.isGameOver()) {
            return;
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        try {
            artemisWorld.dispose();
        } catch (Exception e) {
            Gdx.app.debug(e.getMessage(), e.toString());
        }
    }

}
