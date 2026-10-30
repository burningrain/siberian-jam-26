package com.github.br.libgdx.common.structure;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;

public interface PlatformConfigurator {

    void configureLoaders(AssetManager manager, FileHandleResolver fileHandleResolver);

}
