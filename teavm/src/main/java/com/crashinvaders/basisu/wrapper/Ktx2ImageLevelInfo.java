package com.crashinvaders.basisu.wrapper;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

import java.io.Closeable;

public class Ktx2ImageLevelInfo implements Closeable {

    private JSObject levelInfoJs;

    public Ktx2ImageLevelInfo() {
        this.levelInfoJs = createEmptyJsObject();
    }

    public Ktx2ImageLevelInfo(Object levelInfoJs) {
        this.levelInfoJs = (JSObject) levelInfoJs;
    }

    @Override
    public void close() {
        levelInfoJs = null;
    }

    public int getLayerIndex() {
        return getIntProperty(levelInfoJs, "layerIndex");
    }

    public int getLevelIndex() {
        return getIntProperty(levelInfoJs, "levelIndex");
    }

    public int getOrigWidth() {
        return getIntProperty(levelInfoJs, "origWidth");
    }

    public int getOrigHeight() {
        return getIntProperty(levelInfoJs, "origHeight");
    }

    public int getWidth() {
        return getIntProperty(levelInfoJs, "width");
    }

    public int getHeight() {
        return getIntProperty(levelInfoJs, "height");
    }

    public int getNumBlocksX() {
        return getIntProperty(levelInfoJs, "numBlocksX");
    }

    public int getNumBlocksY() {
        return getIntProperty(levelInfoJs, "numBlocksY");
    }

    public int getTotalBlocks() {
        return getIntProperty(levelInfoJs, "totalBlocks");
    }

    public boolean hasAlphaFlag() {
        return getBoolProperty(levelInfoJs, "alphaFlag");
    }

    @JSBody(script = "return { layerIndex: 0, levelIndex: 0, origWidth: 0, origHeight: 0, width: 0, height: 0, numBlocksX: 0, numBlocksY: 0, totalBlocks: 0, alphaFlag: false };")
    private static native JSObject createEmptyJsObject();

    @JSBody(params = {"obj", "propName"}, script = "return obj && typeof obj[propName] !== 'undefined' ? obj[propName] : 0;")
    private static native int getIntProperty(JSObject obj, String propName);

    @JSBody(params = {"obj", "propName"}, script = "return obj && typeof obj[propName] !== 'undefined' ? !!obj[propName] : false;")
    private static native boolean getBoolProperty(JSObject obj, String propName);

    // Публичный метод, который вы будете вызывать из Java-кода
    public void debugInspectJsObject(String fileInfoContext) {
        logJsObjectStructure(fileInfoContext, this.levelInfoJs);
    }

    @JSBody(params = {"context", "obj"},
        script = "if (!obj) {" +
            "    console.error('[INSPECT] Критическая ошибка: Объект levelInfoJs равен NULL или undefined для контекста:', context);" +
            "    return;" +
            "}" +
            "console.log('%c[INSPECT] Структура Ktx2ImageLevelInfo для ' + context + ':', 'color: #ff00ff; font-weight: bold; font-size: 12px;');" +
            "console.dir(obj);")
    private static native void logJsObjectStructure(String context, JSObject obj);

}
