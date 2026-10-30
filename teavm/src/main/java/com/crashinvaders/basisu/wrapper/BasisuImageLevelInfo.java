package com.crashinvaders.basisu.wrapper;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import java.io.Closeable;

public class BasisuImageLevelInfo implements Closeable {

    private JSObject levelInfoJs;

    // Открываем дефолтный конструктор для безопасного инстанцирования в Java
    public BasisuImageLevelInfo() {
        this.levelInfoJs = createEmptyJsObject();
    }

    public BasisuImageLevelInfo(Object levelInfoJs) {
        this.levelInfoJs = (JSObject) levelInfoJs;
    }

    @Override
    public void close() {
        levelInfoJs = null;
    }

    public int getImageIndex() {
        return getIntProperty(levelInfoJs, "imageIndex");
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

    public int getFirstSliceIndex() {
        return getIntProperty(levelInfoJs, "firstSliceIndex");
    }

    public boolean hasAlphaFlag() {
        return getBoolProperty(levelInfoJs, "alphaFlag");
    }

    public boolean hasIframeFlag() {
        return getBoolProperty(levelInfoJs, "iframeFlag");
    }

    @JSBody(script = "return { imageIndex: 0, levelIndex: 0, origWidth: 0, origHeight: 0, width: 0, height: 0, numBlocksX: 0, numBlocksY: 0, totalBlocks: 0, firstSliceIndex: 0, alphaFlag: false, iframeFlag: false };")
    private static native JSObject createEmptyJsObject();

    @JSBody(params = {"obj", "propName"}, script = "return obj && typeof obj[propName] !== 'undefined' ? obj[propName] : 0;")
    private static native int getIntProperty(JSObject obj, String propName);

    @JSBody(params = {"obj", "propName"}, script = "return obj && typeof obj[propName] !== 'undefined' ? !!obj[propName] : false;")
    private static native boolean getBoolProperty(JSObject obj, String propName);

}
