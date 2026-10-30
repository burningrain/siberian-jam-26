package com.crashinvaders.vfx.gwt;

import static com.badlogic.gdx.graphics.GL20.GL_FRAMEBUFFER_BINDING;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.vfx.gl.VfxGlExtension;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class GwtVfxGlExtension implements VfxGlExtension {
    private static final IntBuffer tmpIntBuf = ByteBuffer.allocateDirect(16 * Integer.SIZE / 8).order(ByteOrder.nativeOrder()).asIntBuffer();

    @Override
    public int getBoundFboHandle() {
        IntBuffer intBuf = tmpIntBuf;
        Gdx.gl.glGetIntegerv(GL_FRAMEBUFFER_BINDING, intBuf);
        return intBuf.get(0);
    }

}
