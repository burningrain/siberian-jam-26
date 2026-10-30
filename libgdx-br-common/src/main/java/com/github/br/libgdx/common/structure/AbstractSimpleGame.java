package com.github.br.libgdx.common.structure;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetErrorListener;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.github.br.libgdx.common.structure.audio.AudioAssetManager;
import com.github.br.libgdx.common.structure.screen.loading.LoadingScreen;
import com.github.br.libgdx.common.structure.screen.loading.SimpleProgressBarImpl;
import com.github.br.libgdx.common.structure.screen.statemachine.GameScreenState;

import java.io.PrintWriter;
import java.io.StringWriter;


public abstract class AbstractSimpleGame<U extends UserFactory> extends ApplicationAdapter {

    private final PlatformConfigurator platformConfigurator;

    private GameSettings gameSettings;
    private AudioSettings audioSettings;
    private SimpleUtils simpleUtils;
    private AssetManager assetManager;
    private AudioAssetManager audioAssetManager;
    private GameManager<U> gameManager;

    public AbstractSimpleGame(PlatformConfigurator platformConfigurator) {
        this.platformConfigurator = platformConfigurator;
    }

    @Override
    public void create() {
        GameSettings.Builder builder = GameSettings.builder();
        fillGameSettings(builder);
        gameSettings = builder.build();
        audioSettings = createAudioSettings();
        simpleUtils = new SimpleUtils(gameSettings);

        InternalFileHandleResolver fileHandleResolver = new InternalFileHandleResolver();
        assetManager = new AssetManager();
        assetManager.setErrorListener(new AssetErrorListener() {
            @Override
            public void error(AssetDescriptor asset, Throwable throwable) {
                Gdx.app.log("error", "--- Asset loading is failed ---");
                Gdx.app.log("error", "File: " + asset.fileName);

                if (throwable != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    throwable.printStackTrace(pw);
                    Gdx.app.log("exception stacktrace: ", sw.toString());
                }
            }
        });

        audioAssetManager = new AudioAssetManager(assetManager, audioSettings);

        initLoaders(assetManager, fileHandleResolver);
        if (platformConfigurator != null) {
            platformConfigurator.configureLoaders(assetManager, fileHandleResolver);
        }

        gameManager = new GameManager<U>();
        gameManager.init(
                gameSettings,
                audioSettings,
                simpleUtils,
                assetManager,
                audioAssetManager,
                createLoadingScreen(),
                createStartState(),
                createUserFactory()
        );

        boolean npotSupported = Gdx.graphics.supportsExtension("GL_OES_texture_npot")
            || Gdx.graphics.supportsExtension("GL_ARB_texture_non_power_of_two");
        Gdx.app.debug("App", "isNpotSupported: " + npotSupported);
    }

    protected abstract U createUserFactory();

    protected LoadingScreen createLoadingScreen() {
        return new LoadingScreen(
                new SimpleProgressBarImpl(
                        gameSettings.getProgressBarWidth(),
                        gameSettings.getProgressBarHeight(),
                        (gameSettings.getVirtualScreenWidth() - gameSettings.getProgressBarWidth()) / 2,
                        (gameSettings.getVirtualScreenHeight() - gameSettings.getProgressBarHeight()) / 2
                ),
                gameSettings.getVirtualScreenWidth(),
                gameSettings.getVirtualScreenHeight()
        );
    }

    protected abstract GameScreenState createStartState();

    protected abstract void initLoaders(AssetManager assetManager, FileHandleResolver fileHandleResolver);

    protected abstract void fillGameSettings(GameSettings.Builder builder);

    private AudioSettings createAudioSettings() {
        return new AudioSettings();
    }

    @Override
    public void render() {
        gameManager.screenStateManager.render(Gdx.graphics.getDeltaTime());
    }

    @Override
    public void resize(int width, int height) {
        gameManager.screenStateManager.resize(width, height);
    }

    @Override
    public void pause() {
        gameManager.screenStateManager.pause();
    }

    @Override
    public void resume() {
        gameManager.screenStateManager.resume();
    }

    @Override
    public void dispose() {
        gameManager.screenStateManager.dispose();
        assetManager.dispose();
    }

}
