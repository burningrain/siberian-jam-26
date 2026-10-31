//package com.github.br.syberian.jam26.screens;
//
//import com.badlogic.gdx.assets.AssetManager;
//import com.badlogic.gdx.assets.loaders.SkinLoader;
//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.TextureAtlas;
//import com.badlogic.gdx.maps.tiled.TiledMap;
//import com.badlogic.gdx.maps.tiled.TmxMapLoader;
//import com.badlogic.gdx.scenes.scene2d.ui.Skin;
//import com.github.br.libgdx.common.structure.screen.loading.AssetsLoader;
//import com.github.br.syberian.jam26.Constants;
//import com.github.br.syberian.jam26.Resources;
//
//import workaround.ktx.tiled.TmxUtils;
//
//public class Level1AssetLoader implements AssetsLoader {
//    @Override
//    public void loadAssets(AssetManager assetManager) {
//        TmxMapLoader.Parameters params = new TmxMapLoader.Parameters();
//
//        // Трилинейная фильтрация для убирания лесенок из картинок
//        params.textureMinFilter = Texture.TextureFilter.Linear;
//        params.textureMagFilter = Texture.TextureFilter.Linear;
//
//        assetManager.load(TmxUtils.getTmx(Resources.Tmx.LEVEL_1, Constants.IS_USE_KTX2), TiledMap.class, params);
//
//        // skin
//        assetManager.load(Resources.SKIN_ATLAS, TextureAtlas.class);
//        assetManager.load(Resources.SKIN, Skin.class, new SkinLoader.SkinParameter(Resources.SKIN_ATLAS));
//
//        // animation
//        //assetManager.load(Resources.ANIMATION_ATLAS, TextureAtlas.class);
//    }
//
//    @Override
//    public void unloadAssets(AssetManager assetManager) {
//        assetManager.unload(TmxUtils.getTmx(Resources.Tmx.LEVEL_1, Constants.IS_USE_KTX2));
//    }
//}
