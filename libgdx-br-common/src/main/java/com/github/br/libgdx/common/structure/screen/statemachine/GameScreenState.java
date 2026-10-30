package com.github.br.libgdx.common.structure.screen.statemachine;

import com.github.br.libgdx.common.structure.screen.AbstractGameScreen;
import com.github.br.libgdx.common.structure.screen.loading.AssetsLoader;

public class GameScreenState {

    public final AbstractGameScreen screen;
    public final AssetsLoader assetsLoader;

    public GameScreenState(AbstractGameScreen screen, AssetsLoader assetsLoader) {
        this.screen = screen;
        this.assetsLoader = assetsLoader;
    }

}
