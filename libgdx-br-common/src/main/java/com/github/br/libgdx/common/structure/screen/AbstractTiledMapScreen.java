package com.github.br.libgdx.common.structure.screen;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.libgdx.common.structure.GameSettings;
import com.github.br.libgdx.common.structure.screen.ui.ActorFactory;
import com.github.br.libgdx.common.structure.screen.ui.CustomOrthogonalTiledMapRenderer;

import workaround.ktx.tiled.TmxUtils;

public abstract class AbstractTiledMapScreen<A extends ActorFactory> extends AbstractGameScreen
    implements RestartScreen {

    private final String tiledMapPath;
    private final String pathToSkin;

    protected TiledMap tiledMap;
    protected CustomOrthogonalTiledMapRenderer renderer;

    protected OrthographicCamera camera;
    protected Viewport viewport;

    protected A actorFactory;

    public AbstractTiledMapScreen(String tiledMapPath, String pathToSkin) {
        this.tiledMapPath = tiledMapPath;
        this.pathToSkin = pathToSkin;
    }

    @Override
    public void restart() {
        getGameManager().screenStateManager.restartCurrentState();
    }

    public abstract void triggerExit();

    @Override
    public void show() {
        GameSettings gameSettings = getGameManager().gameSettings;
        String tiledPath = TmxUtils.getTmx(tiledMapPath, gameSettings.isUsingKtx2());
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
        fixBleeding(tiledMap);
        renderer = new CustomOrthogonalTiledMapRenderer(
            actorFactory,
            viewport,
            tiledMap,
            1f
        );

        afterShow(tiledMap, renderer, viewport, actorFactory);
    }

    protected abstract A createActionFactory(Skin gameSkin, AssetManager assetManager, GameSettings gameSettings);

    protected abstract void afterShow(
        TiledMap tiledMap,
        CustomOrthogonalTiledMapRenderer renderer,
        Viewport viewport,
        A actorFactory
    );

    @Override
    public abstract void render(float delta);

    @Override
    public void resize(int width, int height) {
        GameSettings gameSettings = getGameManager().gameSettings;
        viewport.update(width, height, gameSettings.isCenterCamera());
        renderer.resize(width, height);
    }

    @Override
    public abstract void pause();

    @Override
    public abstract void resume();

    @Override
    public abstract void hide();

    @Override
    public abstract void dispose();

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

}
