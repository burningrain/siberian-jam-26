package com.github.br.syberian.jam26.screens;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.br.libgdx.common.structure.screen.loading.AssetsLoader;
import com.github.br.syberian.jam26.Resources;

public class SettingsScreenLoader implements AssetsLoader {

    @Override
    public void loadAssets(AssetManager assetManager) {
        assetManager.load(Resources.MAIN_MENU_BACKGROUND, Texture.class);
        assetManager.load(Resources.SKIN_ATLAS, TextureAtlas.class);
        assetManager.load(Resources.SKIN, Skin.class, new SkinLoader.SkinParameter(Resources.SKIN_ATLAS));

        assetManager.load(Resources.Music.MENU_THEME, Music.class);
    }

    @Override
    public void unloadAssets(AssetManager assetManager) {
        assetManager.unload(Resources.MAIN_MENU_BACKGROUND);
        assetManager.unload(Resources.SKIN_ATLAS);
        assetManager.unload(Resources.SKIN);

        assetManager.unload(Resources.Music.MENU_THEME);
    }

}
