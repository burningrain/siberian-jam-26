package com.github.br.libgdx.common.structure.screen.ui.vfx;

import com.badlogic.gdx.utils.Array;
import com.crashinvaders.vfx.effects.ChainVfxEffect;

public interface VfxEffectPooledFactory {
    Array<ChainVfxEffect> getEffects(String vfxEffect);

    void update(float deltaTime, String effectTitle);

}
