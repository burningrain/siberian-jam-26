package com.github.br.libgdx.common.structure.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.profiling.GLProfiler;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.libgdx.common.structure.GameSettings;
import com.github.br.libgdx.common.structure.screen.ui.ActorFactory;
import com.github.br.libgdx.common.structure.screen.ui.CustomOrthogonalTiledMapRenderer;
import com.github.br.libgdx.common.structure.screen.ui.vfx.VfxEffectPooledFactory;

import workaround.ktx.tiled.TmxUtils;

public abstract class AbstractTiledMapUiScreen<A extends ActorFactory, V extends VfxEffectPooledFactory> extends AbstractGameScreen {

    private final String tiledMapPath;
    private final String pathToSkin;

    protected TiledMap tiledMap;
    protected CustomOrthogonalTiledMapRenderer renderer;

    protected OrthographicCamera camera;
    protected Viewport viewport;

    protected A actorFactory;

    protected V vfxEffectPoolFactory;
    protected boolean isVfxEnabled;

    private boolean isGlProfileActive;
    private GLProfiler profiler;

    public AbstractTiledMapUiScreen(
        String tiledMapPath,
        String pathToSkin,
        boolean isVfxEnabled,
        boolean isGlProfileActive
    ) {
        this.tiledMapPath = tiledMapPath;
        this.pathToSkin = pathToSkin;
        this.isVfxEnabled = isVfxEnabled;

        this.isGlProfileActive = isGlProfileActive;
    }

    @Override
    public void show() {
        if (isGlProfileActive) {
            profiler = new GLProfiler(Gdx.graphics);
            profiler.enable();
        }
        GameSettings gameSettings = getGameManager().gameSettings;
        String tiledPath = TmxUtils.getTmx(tiledMapPath, gameSettings.isUseKtx2());
        AssetManager assetManager = getGameManager().assetManager;
        tiledMap = assetManager.get(tiledPath);

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(
            gameSettings.getVirtualScreenWidth(),
            gameSettings.getVirtualScreenHeight(),
            camera
        );

        Skin gameSkin = assetManager.get(pathToSkin);
        actorFactory = createActionFactory(gameSkin, assetManager, gameSettings);
        vfxEffectPoolFactory = createVfxEffectPoolFactory(assetManager, gameSettings);
        fixBleeding(tiledMap);
        renderer = new CustomOrthogonalTiledMapRenderer(
            actorFactory,
            viewport,
            tiledMap,
            1f,
            isVfxEnabled,
            vfxEffectPoolFactory,
            getBackBufferWidth(),
            getBackBufferHeight()
        );

        afterShow(tiledMap, renderer, viewport, actorFactory, vfxEffectPoolFactory);
    }

    //TODO быстрофикс - на пиксель растягиваем каждый тайл, чтобы зазоры не были видны
    //TODO я не знаю откуда они!
    public void fixBleeding(TiledMap map) {
        for (TiledMapTileSet tileset : map.getTileSets()) {
            for (TiledMapTile tile : tileset) {
                TextureRegion region = tile.getTextureRegion();
                if (region == null) continue;
                float fixX = 1f / region.getTexture().getWidth();
                float fixY = 1f / region.getTexture().getHeight();
                region.setU(region.getU() + fixX);
                region.setV(region.getV() + fixY);
                region.setU2(region.getU2() + fixX);
                region.setV2(region.getV2() + fixY);
            }
        }
    }

    protected int getBackBufferHeight() {
        return Gdx.graphics.getBackBufferHeight();
    }

    protected int getBackBufferWidth() {
        return Gdx.graphics.getBackBufferWidth();
    }

    protected abstract A createActionFactory(Skin gameSkin, AssetManager assetManager, GameSettings gameSettings);

    protected abstract V createVfxEffectPoolFactory(AssetManager assetManager, GameSettings gameSettings);

    protected abstract void afterShow(
        TiledMap tiledMap,
        CustomOrthogonalTiledMapRenderer renderer,
        Viewport viewport,
        A actorFactory,
        V vfxEffectPoolFactory
    );

    @Override
    public void render(float delta) {
        update(delta, viewport);

        Gdx.gl.glClearColor(1, 0, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        GameSettings gameSettings = getGameManager().gameSettings;
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

        if (isGlProfileActive) {
            String simpleName = this.getClass().getSimpleName();
            Gdx.app.log(simpleName, "Draw Calls: " + profiler.getDrawCalls());
            Gdx.app.log(simpleName, "Texture Bindings: " + profiler.getTextureBindings());
            Gdx.app.log(simpleName, "Shader Switches: " + profiler.getShaderSwitches());
            Gdx.app.log(simpleName, "Vertices: " + profiler.getVertexCount().total);
            Gdx.app.log(simpleName, "\n");

            profiler.reset();
        }
    }

    protected abstract void update(float delta, Viewport viewport);

    @Override
    public void resize(int width, int height) {
        GameSettings gameSettings = getGameManager().gameSettings;
        viewport.update(width, height, gameSettings.isCenterCamera());
        renderer.resize(width, height);
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
    }

}
