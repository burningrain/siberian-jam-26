package com.github.br.libgdx.common.structure.screen.ui.vfx.effect;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.crashinvaders.vfx.VfxRenderContext;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ShaderVfxEffect;
import com.crashinvaders.vfx.framebuffer.VfxFrameBuffer;
import com.crashinvaders.vfx.framebuffer.VfxPingPongWrapper;

public class SmoothVignetteEffect extends ShaderVfxEffect implements ChainVfxEffect {

    private static final String TEXTURE0 = "u_texture0";
    private static final String VIGNETTE_INTENSITY = "u_vignetteIntensity";
    private static final String VIGNETTE_X = "u_vignetteX";
    private static final String VIGNETTE_Y = "u_vignetteY";
    private static final String CENTER_X = "u_centerX";
    private static final String CENTER_Y = "u_centerY";

    private static final String VERTEX_SHADER =
        "attribute vec4 a_position;\n" +
            "attribute vec2 a_texCoord0;\n" +
            "varying vec2 v_texCoords;\n" +
            "void main() {\n" +
            "    v_texCoords = a_texCoord0;\n" +
            "    gl_Position = a_position;\n" +
            "}";

    private static final String FRAGMENT_SHADER =
        "#ifdef GL_ES\n" +
            "precision mediump float;\n" +
            "#endif\n" +
            "varying vec2 v_texCoords;\n" +
            "uniform sampler2D u_texture0;\n" +
            "uniform float u_vignetteIntensity;\n" +
            "uniform float u_vignetteX;\n" +
            "uniform float u_vignetteY;\n" +
            "uniform float u_centerX;\n" +
            "uniform float u_centerY;\n" +
            "\n" +
            "// Генератор псевдослучайного шума высокого разрешения (Interleaved Gradient Noise)\n" +
            "float screenSpaceNoise(vec2 screenPos) {\n" +
            "    return fract(52.51 * fract(dot(screenPos, vec2(0.0605, 0.007))));\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "    vec4 color = texture2D(u_texture0, v_texCoords);\n" +
            "    \n" +
            "    // 1. АБСОЛЮТНО ИДЕНТИЧНАЯ математика оригинала из вашей версии gdx-vfx\n" +
            "    vec2 uv = v_texCoords - vec2(u_centerX, u_centerY);\n" +
            "    float dist = dot(uv, uv) * u_vignetteIntensity;\n" +
            "    float vignette = clamp(1.0 - dist * (u_vignetteX + u_vignetteY * dist), 0.0, 1.0);\n" +
            "    \n" +
            "    // 2. ПРОГРАММНЫЙ ДИЗЕРИНГ ДЛЯ УНИЧТОЖЕНИЯ ЛЕСЕНКИ\n" +
            "    // Добавляем микро-отклонение в пределах 1/255 части кванта 8-битного цвета.\n" +
            "    float noise = (screenSpaceNoise(gl_FragCoord.xy) - 0.5) / 255.0;\n" +
            "    vignette = clamp(vignette + noise, 0.0, 1.0);\n" +
            "    \n" +
            "    // 3. Вывод цвета (оригинальное линейное умножение)\n" +
            "    gl_FragColor = vec4(color.rgb * vignette, color.a);\n" +
            "}";

    private float vignetteX = 0.8f;
    private float vignetteY = 0.25f;
    private float centerX = 0.5f;
    private float centerY = 0.5f;
    private float intensity = 1f;

    public SmoothVignetteEffect() {
        super(new ShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER));
        if (!getProgram().isCompiled()) {
            throw new IllegalArgumentException("Shader compilation failed: " + getProgram().getLog());
        }
        rebind();
    }

    @Override
    public void rebind() {
        super.rebind();
        program.begin();
        program.setUniformi(TEXTURE0, TEXTURE_HANDLE0);
        program.setUniformf(VIGNETTE_INTENSITY, intensity);
        program.setUniformf(VIGNETTE_X, vignetteX);
        program.setUniformf(VIGNETTE_Y, vignetteY);
        program.setUniformf(CENTER_X, centerX);
        program.setUniformf(CENTER_Y, centerY);
        program.end();
    }

    @Override
    public void render(VfxRenderContext context, VfxPingPongWrapper buffers) {
        render(context, buffers.getSrcBuffer(), buffers.getDstBuffer());
    }

    public void render(VfxRenderContext context, VfxFrameBuffer src, VfxFrameBuffer dst) {
        src.getTexture().bind(TEXTURE_HANDLE0);
        renderShader(context, dst);
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
        setUniform(VIGNETTE_INTENSITY, intensity);
    }

    public void setCoords(float x, float y) {
        this.vignetteX = x;
        this.vignetteY = y;
        program.begin();
        program.setUniformf(VIGNETTE_X, x);
        program.setUniformf(VIGNETTE_Y, y);
        program.end();
    }

    public void setVignetteX(float x) {
        this.vignetteX = x;
        setUniform(VIGNETTE_X, x);
    }

    public void setVignetteY(float vignetteY) {
        this.vignetteY = vignetteY;
        setUniform(VIGNETTE_Y, vignetteY);
    }

    public void setCenter(float x, float y) {
        this.centerX = x;
        this.centerY = y;
        program.begin();
        program.setUniformf(CENTER_X, centerX);
        program.setUniformf(CENTER_Y, centerY);
        program.end();
    }

    public float getIntensity() { return intensity; }
    public float getVignetteX() { return vignetteX; }
    public float getVignetteY() { return vignetteY; }
    public float getCenterX() { return centerX; }
    public float getCenterY() { return centerY; }
}
