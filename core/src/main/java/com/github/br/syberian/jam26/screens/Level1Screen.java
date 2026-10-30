package com.github.br.syberian.jam26.screens;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.libgdx.common.structure.GameSettings;
import com.github.br.libgdx.common.structure.screen.AbstractTiledMapUiScreen;
import com.github.br.libgdx.common.structure.screen.ui.CustomOrthogonalTiledMapRenderer;
import com.github.br.syberian.jam26.ui.UserActorFactory;
import com.github.br.syberian.jam26.ui.fx.CustomVfxEffectPooledFactoryImpl;

public class Level1Screen extends AbstractTiledMapUiScreen<UserActorFactory, CustomVfxEffectPooledFactoryImpl> {

    public Level1Screen(
        String tiledMapPath,
        String pathToSkin,
        boolean isVfxEnabled,
        boolean isGlProfileActive
    ) {
        super(tiledMapPath, pathToSkin,isVfxEnabled, isGlProfileActive);
    }

    @Override
    protected UserActorFactory createActionFactory(
        Skin gameSkin,
        AssetManager assetManager,
        GameSettings gameSettings
    ) {
        return new UserActorFactory(gameSkin, assetManager);
    }

    @Override
    protected CustomVfxEffectPooledFactoryImpl createVfxEffectPoolFactory(
        AssetManager assetManager,
        GameSettings gameSettings
    ) {
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

    }

    @Override
    protected void update(float delta, Viewport viewport) {

    }

}
