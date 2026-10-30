package com.crashinvaders.basisu.gdx;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.ArrayBufferView;
import org.teavm.jso.typedarrays.Uint8Array;

import java.nio.Buffer;

/**
 * Мост для WebGL расширений сжатых текстур под TeaVM (бэкенд gdx-teavm).
 * Полностью заменяет GWT-зависимости на прямые JS-вызовы к контексту WebGL.
 */
public class BasisuGdxGl {

    public static void glTexImage2D(int target, int level, int internalFormat,
                                    int width, int height, int border,
                                    int format, int type, Buffer pixels) {
        JSObject glContext = getGlContext();
        if (glContext == null) return;
        // Наш исправленный метод извлечет Uint8Array нужного размера (8388608 байт)
        ArrayBufferView arrayBufferView = getWebGLBufferArray(pixels);
        glTexImage2DNative(glContext, target, level, internalFormat, width, height, border, format, type, arrayBufferView);
    }

    @JSBody(params = { "gl", "target", "level", "internalFormat", "width", "height", "border", "format", "type", "pixels" },
        script = "gl.texImage2D(target, level, internalFormat, width, height, border, format, type, pixels);")
    private static native void glTexImage2DNative(JSObject gl, int target, int level, int internalFormat,
                                                  int width, int height, int border, int format, int type, JSObject pixels);

    public static int[] getSupportedTextureFormats() {
        JSObject glContext = getGlContext();
        if (glContext == null) {
            return new int[0];
        }
        return getSupportedTextureFormatsNative(glContext);
    }

    public static void glCompressedTexImage2D(int target, int level, int internalFormat,
                                              int width, int height, int border,
                                              int imageSize, Buffer pixels) {
        JSObject glContext = getGlContext();
        if (glContext == null) return;

        JSObject arrayBufferView = getWebGLBufferArray(pixels);

        // ВРЕМЕННЫЙ ДИАГНОСТИЧЕСКИЙ ЛОГ: Проверяем размер перед отправкой в GPU
        logWebGLUpload(internalFormat, width, height, imageSize, arrayBufferView);

        glCompressedTexImage2DNative(glContext, target, level, internalFormat, width, height, border, imageSize, arrayBufferView);
    }

    @JSBody(params = {"format", "w", "h", "size", "view"},
        script = "console.log('%c[WebGL ЗАГРУЗКА]', 'color: #00ff00; font-weight: bold;', " +
            "'Формат:', format.toString(16), 'Размер:', w, 'x', h, 'Байт в Java:', size, " +
            "'Байт в JS объекте:', view ? view.byteLength : 'NULL');")
    private static native void logWebGLUpload(int format, int w, int h, int size, JSObject view);

    @JSBody(params = { "gl" }, script =
        // 1. Активируем необходимые WebGL расширения сжатых текстур в контексте браузера
        "gl.getExtension('WEBGL_compressed_texture_astc');" +
            "gl.getExtension('WEBGL_compressed_texture_atc');" +
            "gl.getExtension('WEBGL_compressed_texture_etc');" +
            "gl.getExtension('WEBGL_compressed_texture_etc1');" +
            "gl.getExtension('WEBGL_compressed_texture_pvrtc');" +
            "gl.getExtension('WEBGL_compressed_texture_s3tc');" +

            // 2. Запрашиваем результирующий массив поддерживаемых GPU токенов форматов
            "var supportedFormats = gl.getParameter(0x86A3);" + // GL_COMPRESSED_TEXTURE_FORMATS
            "if (!supportedFormats) return [];" +
            "var resultValues = new Array(supportedFormats.length);" +
            "for (var i = 0; i < supportedFormats.length; i++) {" +
            "    resultValues[i] = supportedFormats[i];" +
            "}" +
            "return resultValues;"
    )
    private static native int[] getSupportedTextureFormatsNative(JSObject gl);

    @JSBody(params = { "gl", "target", "level", "internalFormat", "width", "height", "border", "imageSize", "pixels" },
        script = "gl.compressedTexImage2D(target, level, internalFormat, width, height, border, pixels);")
    private static native void glCompressedTexImage2DNative(JSObject gl, int target, int level, int internalFormat,
                                                            int width, int height, int border, int imageSize, JSObject pixels);

    /**
     * Безопасное динамическое извлечение текущего контекста WebGL из рантайма gdx-teavm.
     */
    @JSBody(params = {}, script =
        // 1. Если контекст уже был где-то кэширован глобально, возвращаем его
        "if (typeof window !== 'undefined' && window.gl) return window.gl;" +

            // 2. Ищем холст строго по вашему id="canvas", затем по тегу
            "var canvas = document.getElementById('canvas') || document.querySelector('canvas');" +
            "if (canvas) {" +
            "    var gl = canvas.getContext('webgl2') || canvas.getContext('webgl') || canvas.getContext('experimental-webgl');" +
            "    if (gl) {" +
            // 3. Лайфхак для gdx-teavm: если бэкенд xpenatan внутри себя будет искать window.gl,
            // сохраняем его туда, чтобы предотвратить появление ошибок "getContext is not defined" в рантайме
            "        if (typeof window !== 'undefined') window.gl = gl;" +
            "        return gl;" +
            "    }" +
            "}" +
            "return null;"
    )
    private static native JSObject getGlContext();

    /**
     * Извлекает корректный JS-массив Uint8Array (ArrayBufferView) с учетом текущей позиции Java-буфера.
     * Полностью адаптировано под WasmGC кучу TeaVM.
     */
    public static ArrayBufferView getWebGLBufferArray(Buffer bufferObj) {
        if (bufferObj == null) return null;

        if (bufferObj instanceof java.nio.ByteBuffer) {
            java.nio.ByteBuffer byteBuffer = (java.nio.ByteBuffer) bufferObj;

            int position = byteBuffer.position();
            int remaining = byteBuffer.remaining();

            // 1. Создаем нативный пустой JS-массив нужного размера байт
            Uint8Array jsBufferView = Uint8Array.create(remaining);

            // 2. Копируем байты из защищенного Java Wasm-объекта в JS-пространство.
            // Чтение байт происходит на стороне Java, что исключает появление undefined в JS.
            for (int i = 0; i < remaining; i++) {
                byte b = byteBuffer.get(position + i);
                setUint8ArrayElement(jsBufferView, i, b & 0xFF); // Маскируем байт в беззнаковое число (0-255)
            }

            return jsBufferView;
        }

        // Если прилетел не ByteBuffer, логируем критическую ошибку структуры
        com.badlogic.gdx.Gdx.app.error("BasisuGdxGl", "Пришедший буфер не поддерживается WasmGC: "
            + bufferObj.getClass().getName());
        return null;
    }

    /**
     * Быстрый нативный сеттер элемента для Uint8Array, полностью совместимый с WasmGC.
     */
    @JSBody(params = {"array", "index", "value"}, script = "array[index] = value;")
    private static native void setUint8ArrayElement(Uint8Array array, int index, int value);

}
