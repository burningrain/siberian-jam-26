package com.github.br.syberian.jam26.screens;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.SkinLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.br.libgdx.common.structure.screen.loading.AssetsLoader;
import com.github.br.syberian.jam26.Constants;
import com.github.br.syberian.jam26.Resources;

import workaround.ktx.tiled.TmxUtils;

public class Level1AssetLoader implements AssetsLoader {

    private final String levelThemeLoopMusic;
    public Level1AssetLoader(String levelThemeLoopMusic) {
        this.levelThemeLoopMusic = levelThemeLoopMusic;
    }

    @Override
    public void loadAssets(AssetManager assetManager) {
        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();

        // Трилинейная фильтрация для убирания лесенок из картинок
        params.textureMinFilter = Texture.TextureFilter.Nearest;
        params.textureMagFilter = Texture.TextureFilter.Nearest;
        assetManager.load(TmxUtils.getTmx(Resources.Tmx.LEVEL_1, Constants.IS_USE_KTX2), TiledMap.class, params);

        // skin
        assetManager.load(Resources.SKIN_ATLAS, TextureAtlas.class);
        assetManager.load(Resources.SKIN, Skin.class, new SkinLoader.SkinParameter(Resources.SKIN_ATLAS));

        assetManager.load(Resources.Atlases.GAME_OBJECTS, TextureAtlas.class);

        // animation
        //assetManager.load(Resources.ANIMATION_ATLAS, TextureAtlas.class);

        // music
        assetManager.load(levelThemeLoopMusic, Music.class);

        // sound
        assetManager.load(Resources.Sound.OPEN_METAL_DOOR, Sound.class);
        assetManager.load(Resources.Sound.CLOSE_METAL_DOOR, Sound.class);
        assetManager.load(Resources.Sound.TALK, Sound.class);
    }

    @Override
    public void unloadAssets(AssetManager assetManager) {
        assetManager.unload(TmxUtils.getTmx(Resources.Tmx.LEVEL_1, Constants.IS_USE_KTX2));
        assetManager.unload(Resources.Atlases.GAME_OBJECTS);

        assetManager.unload(levelThemeLoopMusic);

        assetManager.unload(Resources.Sound.OPEN_METAL_DOOR);
        assetManager.unload(Resources.Sound.CLOSE_METAL_DOOR);
        assetManager.unload(Resources.Sound.TALK);
    }

}
