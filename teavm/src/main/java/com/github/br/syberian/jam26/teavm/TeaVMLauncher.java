package com.github.br.syberian.jam26.teavm;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.crashinvaders.basisu.gdx.Ktx2TextureLoader;
import com.github.br.libgdx.common.structure.PlatformConfigurator;
import com.github.xpenatan.gdx.backends.teavm.TeaApplicationConfiguration;
import com.github.xpenatan.gdx.backends.teavm.TeaApplication;
import com.github.br.syberian.jam26.Main;

import workaround.ktx.tiled.Ktx2AtlasTmxMapLoader;

/**
 * Launches the TeaVM/HTML application.
 */
public class TeaVMLauncher {
    public static void main(String[] args) {
        TeaApplicationConfiguration config = new TeaApplicationConfiguration("canvas");
        //// If width and height are each greater than 0, then the app will use a fixed size.
        //config.width = 640;
        //config.height = 480;
        //// If width and height are both 0, then the app will use all available space.
        config.width = 0;
        config.height = 0;
        new TeaApplication(new Main(new PlatformConfigurator() {
            @Override
            public void configureLoaders(AssetManager assetManager, FileHandleResolver fileHandleResolver) {
                assetManager.setLoader(Texture.class, ".ktx2", new Ktx2TextureLoader(fileHandleResolver));
                assetManager.setLoader(TiledMap.class, new Ktx2AtlasTmxMapLoader(fileHandleResolver));
            }
        }), config);
    }
}
