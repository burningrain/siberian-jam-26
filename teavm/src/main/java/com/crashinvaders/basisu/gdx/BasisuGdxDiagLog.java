package com.crashinvaders.basisu.gdx;

import org.teavm.jso.JSBody;

public class BasisuGdxDiagLog {

    @JSBody(params = {"formatName", "isCompressed", "glFormatHex"},
        script = "console.log('%c[DIAG] Текстура загружается:', 'color: #ffff00; font-weight: bold; font-size: 12px;', " +
            "'Формат Basis:', formatName, " +
            "'Сжатый?:', isCompressed, " +
            "'WebGL Токен Формата:', glFormatHex);")
    public static native void logTextureMeta(String formatName, boolean isCompressed, String glFormatHex);

    @JSBody(params = {"level", "width", "height", "capacity", "remaining"},
        script = "console.log('  -> Mipmap уровень ' + level + ': ' + width + 'x' + height + " +
            "', Размер буфера в Java: ' + capacity + ' байт, remaining: ' + remaining);")
    public static native void logBufferMeta(int level, int width, int height, int capacity, int remaining);

    @JSBody(params = {"isCompressed", "textureTypeHex"},
        script = "if (isCompressed) {" +
            "    console.log('  -> Попытка отправки в BasisuGdxGl.glCompressedTexImage2D...');" +
            "} else {" +
            "    console.log('  -> Попытка отправки в стандартный Gdx.gl.glTexImage2D... Тип текстуры: ' + textureTypeHex);" +
            "}")
    public static native void logBranch(boolean isCompressed, String textureTypeHex);

}
