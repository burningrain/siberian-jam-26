package com.github.ashvard.gdx.simple.animation;

import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

public class SimpleAnimationFsmLoadedCallback implements AssetLoaderParameters.LoadedCallback {

    private final String pathToCommonAtlas; // "orig/pack.atlas"
    private final SimpleAnimationPath[] paths;

    public SimpleAnimationFsmLoadedCallback(String pathToCommonAtlas, SimpleAnimationPath[] paths) {
        this.pathToCommonAtlas = pathToCommonAtlas;
        this.paths = paths;
    }

    @Override
    public void finishedLoading(AssetManager assetManager, String fileName, Class type) {
        TextureAtlas textureAtlas = assetManager.get(pathToCommonAtlas, TextureAtlas.class);
        for (SimpleAnimationPath path : paths) {
            SimpleAnimationSyncLoader.SimpleAnimationParameter parameters = new SimpleAnimationSyncLoader.SimpleAnimationParameter();
            parameters.customTextureAtlas = new CustomTextureAtlas(textureAtlas.findRegions(path.atlasRegionsName));
            assetManager.load(path.pathToAfsmFile, SimpleAnimation.class, parameters);
        }
    }

    public static class SimpleAnimationPath {

        public final String pathToAfsmFile;
        public final String atlasRegionsName;

        public SimpleAnimationPath(String pathToAfsmFile, String atlasRegionsName) {
            this.pathToAfsmFile = pathToAfsmFile;
            this.atlasRegionsName = atlasRegionsName;
        }
    }

    private static class CustomTextureAtlas extends TextureAtlas {

        private final Array<AtlasRegion> regions;

        public CustomTextureAtlas(Array<AtlasRegion> regions) {
            this.regions = regions;
        }

        @Override
        public Array<AtlasRegion> getRegions() {
            return regions;
        }

        @Override
        public void dispose() {
        }

    }
}
