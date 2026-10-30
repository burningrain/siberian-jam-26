package com.github.br.libgdx.common.structure.screen.ui.vfx;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.crashinvaders.vfx.effects.ChainVfxEffect;

public abstract class VfxEffectPooledFactoryImpl implements VfxEffectPooledFactory {

    private final ObjectMap<String, Array<ChainVfxEffect>> effects = new ObjectMap<>();

    @Override
    public Array<ChainVfxEffect> getEffects(String vfxEffect) {
        Array<ChainVfxEffect> chainVfxEffects = effects.get(vfxEffect);
        if (chainVfxEffects == null) {
            throw new IllegalArgumentException("vfxEffect=[" + vfxEffect + "] is not found");
        }

        return chainVfxEffects;
    }

    public void putEffects(String effectName, Array<ChainVfxEffect> chainVfxEffects) {
        effects.put(effectName, chainVfxEffects);
    }

}
