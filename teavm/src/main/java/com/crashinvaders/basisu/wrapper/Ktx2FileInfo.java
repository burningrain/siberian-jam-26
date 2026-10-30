package com.crashinvaders.basisu.wrapper;

import static com.crashinvaders.basisu.wrapper.UniqueIdUtils.findOrThrow;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;

import java.io.Closeable;

public class Ktx2FileInfo implements Closeable {

    private JSObject fileInfoJs;

    // Разрешаем дефолтный конструктор, инициализируя его безопасной JS-пустышкой.
    // Это предотвратит UnsupportedOperationException, когда оригинальная библиотека вызывает new Ktx2FileInfo().
    public Ktx2FileInfo() {
        this.fileInfoJs = createEmptyJsObject();
    }

    public Ktx2FileInfo(Object fileInfoJs) {
        this.fileInfoJs = (JSObject) fileInfoJs;
    }

    // Хелпер для внутренней привязки объекта после нативного вызова
    public void setJsObject(JSObject fileInfoJs) {
        this.fileInfoJs = fileInfoJs;
    }

    @Override
    public void close() {
        // Процедурный JS-объект не требует вызова .delete(), память очистит GC браузера.
        fileInfoJs = null;
    }

    public int getTotalLayers() {
        int layers = getIntProperty(fileInfoJs, "layers");
        if (layers == 0) layers = getIntProperty(fileInfoJs, "totalLayers");
        if (layers == 0) layers = getIntProperty(fileInfoJs, "layerIndex"); // на случай плоского объекта
        return layers;
    }

    public int getTotalMipmapLevels() {
        int levels = getIntProperty(fileInfoJs, "mipmapLevels");
        if (levels == 0) levels = getIntProperty(fileInfoJs, "totalMipmapLevels");
        if (levels == 0) levels = getIntProperty(fileInfoJs, "levelIndex");
        return levels;
    }

    public int getImageWidth() {
        int w = getIntProperty(fileInfoJs, "width");
        if (w == 0) w = getIntProperty(fileInfoJs, "imageWidth");
        if (w == 0) w = getIntProperty(fileInfoJs, "origWidth");
        return w;
    }

    public int getImageHeight() {
        int h = getIntProperty(fileInfoJs, "height");
        if (h == 0) h = getIntProperty(fileInfoJs, "imageHeight");
        if (h == 0) h = getIntProperty(fileInfoJs, "origHeight");
        return h;
    }

    public boolean hasAlpha() {
        // Проверяем все возможные варианты названия флага альфы в разных версиях враппера
        return getBoolProperty(fileInfoJs, "hasAlpha")
            || getBoolProperty(fileInfoJs, "alphaFlag")
            || getBoolProperty(fileInfoJs, "hasAlphaSlices");
    }

    public BasisuTextureFormat getTextureFormat() {
        int textureFormatId = getIntProperty(fileInfoJs, "textureFormat");
        return findOrThrow(BasisuTextureFormat.values(), textureFormatId);
    }

    // --- ES5-совместимые нативные методы доступа к свойствам плоского JS-объекта ---

    @JSBody(script = "return { totalLayers: 1, totalMipmapLevels: 1, imageWidth: 0, imageHeight: 0, hasAlpha: false, textureFormat: 0 };")
    private static native JSObject createEmptyJsObject();

    @JSBody(params = {"obj", "propName"}, script = "return obj && typeof obj[propName] !== 'undefined' ? obj[propName] : 0;")
    private static native int getIntProperty(JSObject obj, String propName);

    @JSBody(params = {"obj", "propName"}, script = "return obj && typeof obj[propName] !== 'undefined' ? !!obj[propName] : false;")
    private static native boolean getBoolProperty(JSObject obj, String propName);
}
