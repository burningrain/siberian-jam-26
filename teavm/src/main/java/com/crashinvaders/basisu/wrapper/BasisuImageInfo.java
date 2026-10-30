package com.crashinvaders.basisu.wrapper;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

import java.io.Closeable;

public class BasisuImageInfo implements Closeable {
    private JSObject imageInfoJs;

    public BasisuImageInfo() {
        this.imageInfoJs = createEmptyJsObject();
    }

    public BasisuImageInfo(Object imageInfoJs) {
        this.imageInfoJs = (JSObject) imageInfoJs;
    }

    @Override
    public void close() {
        imageInfoJs = null;
    }

    public int getImageIndex() {
        return getIntProperty(imageInfoJs, "imageIndex");
    }

    public int getTotalLevels() {
        return getIntProperty(imageInfoJs, "totalLevels");
    }

    public int getOrigWidth() {
        return getIntProperty(imageInfoJs, "origWidth");
    }

    public int getOrigHeight() {
        return getIntProperty(imageInfoJs, "origHeight");
    }

    public int getWidth() {
        return getIntProperty(imageInfoJs, "width");
    }

    public int getHeight() {
        return getIntProperty(imageInfoJs, "height");
    }

    public int getNumBlocksX() {
        return getIntProperty(imageInfoJs, "numBlocksX");
    }

    public int getNumBlocksY() {
        return getIntProperty(imageInfoJs, "numBlocksY"); // ИСПРАВЛЕНО: используем правильный imageInfoJs
    }

    public int getTotalBlocks() {
        return getIntProperty(imageInfoJs, "totalBlocks");
    }

    public int getFirstSliceIndex() {
        return getIntProperty(imageInfoJs, "firstSliceIndex");
    }

    public boolean hasAlphaFlag() {
        return getBoolProperty(imageInfoJs, "alphaFlag");
    }

    public boolean hasIframeFlag() {
        return getBoolProperty(imageInfoJs, "iframeFlag");
    }

    @JSBody(script = "return { imageIndex: 0, totalLevels: 1, origWidth: 0, origHeight: 0, width: 0, height: 0, numBlocksX: 0, numBlocksY: 0, totalBlocks: 0, firstSliceIndex: 0, alphaFlag: false, iframeFlag: false };")
    private static native JSObject createEmptyJsObject();

    @JSBody(params = {"obj", "propName"}, script = "return obj && typeof obj[propName] !== 'undefined' ? obj[propName] : 0;")
    private static native int getIntProperty(JSObject obj, String propName);

    @JSBody(params = {"obj", "propName"}, script = "return obj && typeof obj[propName] !== 'undefined' ? !!obj[propName] : false;")
    private static native boolean getBoolProperty(JSObject obj, String propName);

}
