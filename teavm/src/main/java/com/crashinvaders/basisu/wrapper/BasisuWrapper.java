package com.crashinvaders.basisu.wrapper;

import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.ArrayBufferView;
import org.teavm.jso.typedarrays.Uint8Array;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BasisuWrapper {

    public static void disposeNativeBuffer(ByteBuffer dataBuffer) {
        // Память в JS/TeaVM контролируется GC, явная очистка указателей не требуется
    }

    public static void releaseEncodedData(Buffer data) {
        // Метод-заглушка для обратной совместимости API
    }

    public static boolean isTranscoderTexFormatSupported(BasisuTranscoderTextureFormat transcoderTexFormat, BasisuTextureFormat basisTexFormat) {
        if (transcoderTexFormat == null || basisTexFormat == null) {
            throw new GdxRuntimeException("[BasisuWrapper.isTranscoderTexFormatSupported] Переданы null-параметры форматов!");
        }
        return isTranscoderTexFormatSupportedNative(transcoderTexFormat.getId(), basisTexFormat.getId());
    }

    @JSBody(params = {"transcoderTexFormatId", "basisTexFormatId"},
        script = "var mod = window.basisuModule || window.Module;" +
            "if (!mod) throw new Error('[WASM_FATAL] window.basisuModule или window.Module не инициализирован при проверке формата!');" +
            "if (typeof mod.isTranscoderTexFormatSupported !== 'function') throw new Error('[WASM_FATAL] Метод mod.isTranscoderTexFormatSupported отсутствует в WebAssembly-модуле!');" +

            "var canvas = document.getElementById('canvas') || document.getElementsByTagName('canvas')[0];" +
            "if (!canvas) throw new Error('[WebGL_FATAL] Элемент canvas не найден в DOM при проверке форматов!');" +
            "var gl = canvas.getContext('webgl2') || canvas.getContext('webgl') || canvas.getContext('experimental-webgl');" +
            "if (!gl) throw new Error('[WebGL_FATAL] Не удалось получить контекст WebGL при проверке форматов!');" +

            "var nativeSupported = mod.isTranscoderTexFormatSupported(transcoderTexFormatId, basisTexFormatId);" +
            "if (!nativeSupported) return false;" +

            "if (transcoderTexFormatId === 0 || transcoderTexFormatId === 1) return !!(gl.getExtension('WEBGL_compressed_texture_etc1') || gl.getExtension('WEBGL_compressed_texture_etc'));" +
            "if (transcoderTexFormatId === 2) return !!gl.getExtension('WEBGL_compressed_texture_etc');" +
            "if (transcoderTexFormatId === 3 || transcoderTexFormatId === 4) return !!(gl.getExtension('WEBGL_compressed_texture_s3tc') || gl.getExtension('WEBGL_COMPRESSED_TEXTURE_S3TC') || gl.getExtension('WEBGL_compressed_texture_s3tc_srgb'));" +
            "if (transcoderTexFormatId === 7) return !!gl.getExtension('WEBGL_compressed_texture_astc');" +
            "if (transcoderTexFormatId === 17 || transcoderTexFormatId === 14 || transcoderTexFormatId === 18 || transcoderTexFormatId === 20) return true;" +
            "return false;")
    private static native boolean isTranscoderTexFormatSupportedNative(int transcoderTexFormatId, int basisTexFormatId);

    public static boolean basisValidateHeader(Buffer data) {
        validateJavaBuffer("basisValidateHeader", data);
        return basisValidateHeaderNative(prepareCleanArray(toTypedArray(data)));
    }

    @JSBody(params = {"bytes"}, script =
        "var mod = window.basisuModule || window.Module;" +
            "if (!mod) throw new Error('[WASM_FATAL] Модуль basisuModule не найден при вызове basisValidateHeader!');" +
            "if (typeof mod.basisValidateHeader !== 'function') throw new Error('[WASM_FATAL] Метод basisValidateHeader отсутствует в WASM!');" +
            "return !!mod.basisValidateHeader(bytes);")
    private static native boolean basisValidateHeaderNative(JSObject bytes);

    public static boolean basisValidateChecksum(Buffer data, boolean fullValidation) {
        validateJavaBuffer("basisValidateChecksum", data);
        return basisValidateChecksumNative(prepareCleanArray(toTypedArray(data)), fullValidation);
    }

    @JSBody(params = {"bytes", "fullValidation"}, script =
        "var mod = window.basisuModule || window.Module;" +
            "if (!mod) throw new Error('[WASM_FATAL] Модуль basisuModule не найден при вызове basisValidateChecksum!');" +
            "if (typeof mod.basisValidateChecksum !== 'function') throw new Error('[WASM_FATAL] Метод basisValidateChecksum отсутствует в WASM!');" +
            "return !!mod.basisValidateChecksum(bytes, fullValidation);")
    private static native boolean basisValidateChecksumNative(JSObject bytes, boolean fullValidation);

    // --- BASIS СЕКЦИЯ МЕТАДАННЫХ И ТРАНСКОДИРОВАНИЯ ---

    public static BasisuFileInfo basisGetFileInfo(Buffer data) {
        validateJavaBuffer("basisGetFileInfo", data);
        JSObject jsInfo = basisGetFileInfoNative(prepareCleanArray(toTypedArray(data)));
        if (jsInfo == null) {
            throw new GdxRuntimeException("[BasisuWrapper.basisGetFileInfo] WebAssembly вернул NULL вместо объекта BasisuFileInfo!");
        }
        return new BasisuFileInfo(jsInfo);
    }

    @JSBody(params = {"bytes"}, script =
        "var mod = window.basisuModule || window.Module;" +
            "if (!mod) throw new Error('[WASM_FATAL] Модуль basisuModule не найден при вызове basisGetFileInfo!');" +
            "if (typeof mod.basisGetFileInfo !== 'function') throw new Error('[WASM_FATAL] Метод basisGetFileInfo отсутствует в WASM!');" +
            "return mod.basisGetFileInfo(bytes);")
    private static native JSObject basisGetFileInfoNative(JSObject bytes);

    public static BasisuFileInfo basisGetImageInfo(Buffer data, int imageIndex) {
        validateJavaBuffer("basisGetImageInfo", data);
        return basisGetFileInfo(data); // Совместимый агрегированный вызов метаданных
    }

    public static BasisuImageLevelInfo basisGetImageLevelInfo(Buffer data, int imageIndex, int imageLevel) {
        validateJavaBuffer("basisGetImageLevelInfo", data);
        JSObject jsInfo = basisGetImageLevelInfoNative(prepareCleanArray(toTypedArray(data)), imageIndex, imageLevel);
        if (jsInfo == null) {
            throw new GdxRuntimeException("[BasisuWrapper.basisGetImageLevelInfo] WebAssembly вернул NULL для imageIndex=" + imageIndex + ", level=" + imageLevel);
        }
        return new BasisuImageLevelInfo(jsInfo);
    }

    @JSBody(params = {"bytes", "imageIndex", "imageLevel"}, script =
        "var mod = window.basisuModule || window.Module;" +
            "if (!mod) throw new Error('[WASM_FATAL] Модуль basisuModule не найден при вызове basisGetImageLevelInfo!');" +
            "if (typeof mod.basisGetImageLevelInfo !== 'function') throw new Error('[WASM_FATAL] Метод basisGetImageLevelInfo отсутствует в WASM!');" +
            "return mod.basisGetImageLevelInfo(bytes, imageIndex, imageLevel);")
    private static native JSObject basisGetImageLevelInfoNative(JSObject bytes, int imageIndex, int imageLevel);

    public static ByteBuffer basisTranscode(Buffer data, int imageIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        validateJavaBuffer("basisTranscode", data);
        if (textureFormat == null)
            throw new GdxRuntimeException("[BasisuWrapper.basisTranscode] Передан null-формат транскодирования!");

        JSObject srcBytes = prepareCleanArray(toTypedArray(data));
        int formatId = textureFormat.getId();

        int expectedSize = basisGetTranscodedImageSizeNative(srcBytes, imageIndex, levelIndex, formatId);
        if (expectedSize <= 0) {
            throw new GdxRuntimeException("[BasisuWrapper.basisTranscode] Нативный модуль вернул некорректный размер буфера: " + expectedSize + " для уровня: " + levelIndex);
        }

        ByteBuffer outBuffer = BufferUtils.newByteBuffer(expectedSize);
        outBuffer.order(ByteOrder.nativeOrder());

        Uint8Array dstBytes = Uint8Array.create(getBufferArray(outBuffer), getBufferOffset(outBuffer), expectedSize);
        boolean success = basisTranscodeNative(srcBytes, imageIndex, levelIndex, formatId, dstBytes);
        if (!success) {
            throw new GdxRuntimeException("[BasisuWrapper.basisTranscode] Фатальный сбой транскодирования Basis внутри WebAssembly (imageIndex: " + imageIndex + ", level: " + levelIndex + ")");
        }

        outBuffer.limit(expectedSize);
        outBuffer.position(0);
        return outBuffer;
    }

    @JSBody(params = {"bytes", "imgIdx", "lvlIdx", "fmt"}, script =
        "var mod = window.basisuModule || window.Module; return mod && typeof mod.basisGetTranscodedImageSize === 'function' ? mod.basisGetTranscodedImageSize(bytes, imgIdx, lvlIdx, fmt) : -1;")
    private static native int basisGetTranscodedImageSizeNative(JSObject bytes, int imgIdx, int lvlIdx, int fmt);

    @JSBody(params = {"bytes", "imgIdx", "lvlIdx", "fmt", "dst"}, script =
        "var mod = window.basisuModule || window.Module; return mod && typeof mod.basisTranscode === 'function' ? !!mod.basisTranscode(bytes, imgIdx, lvlIdx, fmt, dst) : false;")
    private static native boolean basisTranscodeNative(JSObject bytes, int imgIdx, int lvlIdx, int fmt, Uint8Array dst);

    // --- KTX2 СЕКЦИЯ МЕТАДАННЫХ И ТРАНСКОДИРОВАНИЯ ---

    public static Ktx2FileInfo ktx2GetFileInfo(Buffer data) {
        validateJavaBuffer("ktx2GetFileInfo", data);

        JSObject srcBytes = prepareCleanArray(toTypedArray(data));
        JSObject jsInfo = ktx2GetFileInfoNative(srcBytes);

        if (jsInfo == null) {
            throw new GdxRuntimeException("[BasisuWrapper.ktx2GetFileInfo] WebAssembly вернул NULL вместо валидного объекта Ktx2FileInfo!");
        }

        Ktx2FileInfo fileInfo = new Ktx2FileInfo(jsInfo);
        if (fileInfo.getImageWidth() == 0 || fileInfo.getImageHeight() == 0) {
            throw new GdxRuntimeException("[BasisuWrapper.ktx2GetFileInfo] Критическая ошибка: распарсенный KTX2 файл имеет геометрию 0x0! Файл поврежден или пуст!");
        }
        return fileInfo;
    }

    @JSBody(params = {"cleanBytes"}, script =
        "var mod = window.basisuModule || window.Module;" +
            "if (!mod) throw new Error('[WASM_FATAL] Модуль basisuModule не найден при вызове ktx2GetFileInfo!');" +
            "if (typeof mod.ktx2GetFileInfo !== 'function') throw new Error('[WASM_FATAL] Метод mod.ktx2GetFileInfo отсутствует в WebAssembly!');" +
            "var res = mod.ktx2GetFileInfo(cleanBytes);" +
            "return res;")
    private static native JSObject ktx2GetFileInfoNative(JSObject cleanBytes);

    public static Ktx2ImageLevelInfo ktx2GetImageLevelInfo(Buffer data, int layerIndex, int imageLevel) {
        validateJavaBuffer("ktx2GetImageLevelInfo", data);
        JSObject srcBytes = prepareCleanArray(toTypedArray(data));
        JSObject jsInfo = ktx2GetImageLevelInfoNative(srcBytes, layerIndex, imageLevel);
        if (jsInfo == null) {
            throw new GdxRuntimeException("[BasisuWrapper.ktx2GetImageLevelInfo] WebAssembly вернул NULL для KTX2 уровня (layer: " + layerIndex + ", level: " + imageLevel + ")");
        }
        return new Ktx2ImageLevelInfo(jsInfo);
    }

    @JSBody(params = {"cleanBytes", "layerIndex", "levelIndex"},
        script = "var mod = window.basisuModule || window.Module; " +
            "if (!mod) throw new Error('[WASM_FATAL] Модуль basisuModule не найден при вызове ktx2GetImageLevelInfo!');" +
            "if (typeof mod.ktx2GetImageLevelInfo !== 'function') throw new Error('[WASM_FATAL] Метод ktx2GetImageLevelInfo отсутствует в WebAssembly!');" +
            "var res = mod.ktx2GetImageLevelInfo(cleanBytes, layerIndex, levelIndex);" +
            "console.log('[WASM_INFO] Вызов ktx2GetImageLevelInfo в C++:', " +
            "    'Байт передано:', cleanBytes ? cleanBytes.byteLength : 0, " +
            "    'Результат JS:', res);" +
            "return res;")
    private static native JSObject ktx2GetImageLevelInfoNative(JSObject cleanBytes, int layerIndex, int levelIndex);

    public static ByteBuffer ktx2Transcode(Buffer data, int layerIndex, int levelIndex, BasisuTranscoderTextureFormat textureFormat) {
        if (data == null)
            throw new GdxRuntimeException("[BasisuWrapper.ktx2Transcode] Переданный буфер data равен null!");
        if (textureFormat == null)
            throw new GdxRuntimeException("[BasisuWrapper.ktx2Transcode] Параметр textureFormat равен null!");

        // 1. Экспортируем байты KTX2 файла из WasmGC в JS
        ArrayBufferView typedArray = toTypedArray(data);
        JSObject cleanBytes = prepareCleanArray(typedArray);

        // 2. Вызываем WebAssembly-транскодер, который сам создает и возвращает Uint8Array нужной длины
        Uint8Array transcodedJsArray = ktx2TranscodeNative(cleanBytes, layerIndex, levelIndex, textureFormat.getId());

        if (transcodedJsArray == null || transcodedJsArray.getLength() == 0) {
            throw new GdxRuntimeException("[BasisuWrapper.ktx2Transcode] Нативный транскодер KTX2 вернул пустой массив для уровня: " + levelIndex);
        }

        // 3. Узнаем точный размер результирующего массива из JavaScript
        int resultSizeInBytes = transcodedJsArray.getLength();

        // 4. Выделяем стандартный честный ByteBuffer в памяти TeaVM
        ByteBuffer outBuffer = BufferUtils.newByteBuffer(resultSizeInBytes);
        outBuffer.order(ByteOrder.nativeOrder());

        // 5. ЧИСТЫЙ ПЕРЕНОС ПАМЯТИ В JAVA (БЕЗ КОСТЫЛЕЙ):
        // Копируем примитивы через Wasm-границу с помощью нативного геттера.
        // Это гарантирует, что outBuffer будет валидным Java-объектом, готовым для WebGL.
        for (int i = 0; i < resultSizeInBytes; i++) {
            int byteValue = getJsArrayByte(transcodedJsArray, i);
            outBuffer.put(i, (byte) byteValue);
        }

        outBuffer.limit(resultSizeInBytes);
        outBuffer.position(0);

        return outBuffer;
    }

    @JSBody(params = {"cleanBytes", "layerIndex", "levelIndex", "textureFormat"},
        script = "var mod = window.basisuModule || window.Module;" +
            "if (!mod || !mod.ktx2Transcode) throw new Error('[WASM_FATAL] Метод ktx2Transcode отсутствует в WASM модуле!');" +
            "return mod.ktx2Transcode(cleanBytes, layerIndex, levelIndex, textureFormat);")
    private static native Uint8Array ktx2TranscodeNative(JSObject cleanBytes, int layerIndex, int levelIndex, int textureFormat);

    @JSBody(params = {"array", "index"}, script = "return array[index] & 0xFF;")
    private static native int getJsArrayByte(Uint8Array array, int index);

    @JSBody(params = {"bytes", "layerIdx", "lvlIdx", "fmt", "dst"}, script =
        "var mod = window.basisuModule || window.Module; return mod && typeof mod.ktx2Transcode === 'function' ? !!mod.ktx2Transcode(bytes, layerIdx, lvlIdx, fmt, dst) : false;")
    private static native boolean ktx2TranscodeNative(JSObject bytes, int layerIdx, int lvlIdx, int fmt, Uint8Array dst);

    // --- НАДЕЖНЫЙ СЛОЙ ЭКСПОРТА ПАМЯТИ ПОД WASM-GC ---
    private static org.teavm.jso.typedarrays.ArrayBufferView toTypedArray(Buffer data) {
        ByteBuffer byteBuffer = (ByteBuffer) data;
        int remaining = byteBuffer.remaining();
        int oldPos = byteBuffer.position();
        Uint8Array jsArray = Uint8Array.create(remaining);
        // Строгий побайтовый перенос примитивов через Wasm-границу.
        // Исключает чтение неопределенных JS-свойств обфусцированных буферов.
        for (int i = 0; i < remaining; i++) {
            byte b = byteBuffer.get(oldPos + i);
            setJsArrayByte(jsArray, i, b & 0xFF);
        }
        return jsArray;
    }

    @JSBody(params = {"array", "index", "value"}, script = "array[index] = value;")
    private static native void setJsArrayByte(Uint8Array array, int index, int value);

    @JSBody(params = {"data"}, script =
        "if (!data) throw new Error('[WASM_FATAL] Передан null/undefined ArrayBufferView в prepareCleanArray!');" +
            "return data;")
    private static native JSObject prepareCleanArray(ArrayBufferView data);

    @JSBody(params = {"buf"}, script = "if(!buf || (!buf.buffer && (!buf.array || !buf.array.buffer))) throw new Error('[WASM_FATAL] Не удалось извлечь ArrayBuffer из кучи Java!'); return buf.buffer || buf.array.buffer;")
    private static native org.teavm.jso.typedarrays.ArrayBuffer getBufferArray(Object buf);

    @JSBody(params = {"buf"}, script = "return buf && (typeof buf.byteOffset === 'number' ? buf.byteOffset : (buf.array ? buf.array.byteOffset : 0)) || 0;")
    private static native int getBufferOffset(Object buf);

    private static void validateJavaBuffer(String methodName, Buffer data) {
        if (data == null) {
            throw new GdxRuntimeException("[BasisuWrapper." + methodName + "] Критическая ошибка: Переданный буфер данных равен null!");
        }
        if (data.remaining() == 0) {
            throw new GdxRuntimeException("[BasisuWrapper." + methodName + "] Критическая ошибка: Передан пустой буфер (remaining == 0)!");
        }
    }

}
