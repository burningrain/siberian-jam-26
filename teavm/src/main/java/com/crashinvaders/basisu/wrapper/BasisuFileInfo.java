package com.crashinvaders.basisu.wrapper;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.Uint8Array;
import java.io.Closeable;
import static com.crashinvaders.basisu.wrapper.UniqueIdUtils.findOrThrow;

public class BasisuFileInfo implements Closeable {
    private JSObject fileInfoJs;

    // Открываем дефолтный конструктор, чтобы TeaVM WasmGC не кидал UnsupportedOperationException
    public BasisuFileInfo() {
        this.fileInfoJs = createEmptyJsObject();
    }

    public BasisuFileInfo(Object fileInfoJs) {
        this.fileInfoJs = (JSObject) fileInfoJs;
    }

    @Override
    public void close() {
        fileInfoJs = null;
    }

    public BasisuTextureType getTextureType() {
        // Читаем значение перечисления напрямую по числовому полю или вложенному свойству .value
        int textureTypeId = getNestedIntProperty(fileInfoJs, "textureType");
        return findOrThrow(BasisuTextureType.values(), textureTypeId);
    }

    public BasisuTextureFormat getTextureFormat() {
        int textureFormatId = getNestedIntProperty(fileInfoJs, "textureFormat");
        return findOrThrow(BasisuTextureFormat.values(), textureFormatId);
    }

    public int[] getImageMipmapLevels() {
        Uint8Array typedArray = getTypedArrayProperty(fileInfoJs, "imageMipmapLevels");
        if (typedArray == null) {
            return new int[0];
        }
        int[] result = new int[typedArray.getLength()];
        for (int i = 0; i < result.length; i++) {
            result[i] = typedArray.get(i);
        }
        return result;
    }

    public int getVersion() {
        return getIntProperty(fileInfoJs, "version");
    }

    public int getTotalHeaderSize() {
        return getIntProperty(fileInfoJs, "totalHeaderSize");
    }

    public int getTotalSelectors() {
        return getIntProperty(fileInfoJs, "totalSelectors");
    }

    public int getSelectorCodebookSize() {
        return getIntProperty(fileInfoJs, "selectorCodebookSize");
    }

    public int getTotalEndpoints() {
        return getIntProperty(fileInfoJs, "totalEndpoints");
    }

    public int getEndpointCodebookSize() {
        return getIntProperty(fileInfoJs, "endpointCodebookSize");
    }

    public int getTablesSize() {
        return getIntProperty(fileInfoJs, "tablesSize");
    }

    public int getSlicesSize() {
        return getIntProperty(fileInfoJs, "slicesSize");
    }

    public int getUsPerFrame() {
        return getIntProperty(fileInfoJs, "usPerFrame");
    }

    public int getTotalImages() {
        return getIntProperty(fileInfoJs, "totalImages");
    }

    public int getUserdata0() {
        return getIntProperty(fileInfoJs, "userdata0");
    }

    public int getUserdata1() {
        return getIntProperty(fileInfoJs, "userdata1");
    }

    public boolean isFlippedY() {
        return getBoolProperty(fileInfoJs, "yFlipped");
    }

    public boolean isEtc1s() {
        return getBoolProperty(fileInfoJs, "etc1s");
    }

    public boolean hasAlphaSlices() {
        return getBoolProperty(fileInfoJs, "hasAlphaSlices");
    }

    // --- ES5-совместимые нативные JSBody методы подмены рантайма ---

    @JSBody(script = "return { textureType: 0, textureFormat: 0, version: 0, totalHeaderSize: 0, totalSelectors: 0, selectorCodebookSize: 0, totalEndpoints: 0, endpointCodebookSize: 0, tablesSize: 0, slicesSize: 0, usPerFrame: 0, totalImages: 1, userdata0: 0, userdata1: 0, yFlipped: false, etc1s: false, hasAlphaSlices: false };")
    private static native JSObject createEmptyJsObject();

    @JSBody(params = {"obj", "propName"}, script = "return obj && typeof obj[propName] !== 'undefined' ? obj[propName] : 0;")
    private static native int getIntProperty(JSObject obj, String propName);

    @JSBody(params = {"obj", "propName"}, script = "return obj && typeof obj[propName] !== 'undefined' ? !!obj[propName] : false;")
    private static native boolean getBoolProperty(JSObject obj, String propName);

    // Безопасное извлечение значения перечисления (поддерживает как плоский {prop: 2}, так и объектный {prop: {value: 2}} стиль)
    @JSBody(params = {"obj", "propName"},
        script = "if (!obj || typeof obj[propName] === 'undefined') return 0;" +
            "var val = obj[propName];" +
            "if (val && typeof val === 'object' && typeof val.value !== 'undefined') return val.value;" +
            "if (val && typeof val.getValue === 'function') return val.getValue();" +
            "return typeof val === 'number' ? val : 0;")
    private static native int getNestedIntProperty(JSObject obj, String propName);

    @JSBody(params = {"obj", "propName"}, script = "return obj && obj[propName] ? obj[propName] : null;")
    private static native Uint8Array getTypedArrayProperty(JSObject obj, String propName);

}
