package com.github.br.syberian.jam26.ui.fx;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.crashinvaders.vfx.effects.BloomEffect;
import com.crashinvaders.vfx.effects.ChainVfxEffect;
import com.crashinvaders.vfx.effects.ChromaticAberrationEffect;
import com.crashinvaders.vfx.effects.CrtEffect;
import com.crashinvaders.vfx.effects.FilmGrainEffect;
import com.crashinvaders.vfx.effects.FisheyeEffect;
import com.crashinvaders.vfx.effects.FxaaEffect;
import com.crashinvaders.vfx.effects.GaussianBlurEffect;
import com.crashinvaders.vfx.effects.LensFlareEffect;
import com.crashinvaders.vfx.effects.LevelsEffect;
import com.crashinvaders.vfx.effects.MotionBlurEffect;
import com.crashinvaders.vfx.effects.NfaaEffect;
import com.crashinvaders.vfx.effects.OldTvEffect;
import com.crashinvaders.vfx.effects.RadialBlurEffect;
import com.crashinvaders.vfx.effects.RadialDistortionEffect;
import com.crashinvaders.vfx.effects.VignettingEffect;
import com.crashinvaders.vfx.effects.WaterDistortionEffect;
import com.crashinvaders.vfx.effects.ZoomEffect;
import com.crashinvaders.vfx.effects.util.GammaThresholdEffect;
import com.github.br.libgdx.common.structure.screen.ui.vfx.VfxEffectPooledFactoryImpl;
import com.github.br.libgdx.common.structure.screen.ui.vfx.effect.SmoothVignetteEffect;

public class CustomVfxEffectPooledFactoryImpl extends VfxEffectPooledFactoryImpl {

    public interface Title {

        String MAIN_VFX = "MAIN_VFX";
        String HELL_VFX = "HELL_VFX";
        String THINKING_VFX = "THINKING_VFX";


    }

    private float timeAccumulator = 0f;

    private void updateHell(float deltaTime) {
        // Настройки вибрации:
        float baseShift = 1.0f;      // Минимальный/базовый сдвиг в пикселях
        float amplitude = 0.2f;      // Насколько сильно отклоняется (в пикселях)
        float speed = 8.5f;         // Скорость вибрации (частота)

        // 1. Вариант: Плавная пульсация (синусоида)
        // Математика выдаст значение от (baseShift) до (baseShift + amplitude)
        float currentShift = baseShift + MathUtils.sin(timeAccumulator * speed) * amplitude;
        hellChromaticAberrationEffect.setMaxDistortion(MathUtils.clamp(currentShift, 0.1f, 1.2f));

        float light = MathUtils.clamp(currentShift, 0.15f, 0.3f);
        lensFlareEffect.setLightPosition(light * MathUtils.cos(timeAccumulator), light * MathUtils.sin(timeAccumulator));
        lensFlareEffect.setIntensity(MathUtils.clamp(currentShift * 2, 0.1f, 5.0f));

        vignettingEffect.setIntensity(0.9f);

        waterDistortionEffect.setSpeed(0.8f);
        waterDistortionEffect.setTime(timeAccumulator);
        waterDistortionEffect.setAmount(4);

        radialBlurEffect.setStrength(MathUtils.clamp(MathUtils.cos(timeAccumulator), -0.5f, 0));
    }

    private void updateMainVfx(float deltaTime) {
        vignettingEffect.setTime(timeAccumulator);
    }

    private void updateThinking(float deltaTime) {
        // Настройки вибрации:
        float baseShift = 1.0f;      // Минимальный/базовый сдвиг в пикселях
        float amplitude = 1.0f;      // Насколько сильно отклоняется (в пикселях)
        float speed = 1.5f;         // Скорость вибрации (частота)

        // 1. Вариант: Плавная пульсация (синусоида)
        // Математика выдаст значение от (baseShift) до (baseShift + amplitude)
        float currentShift = baseShift + MathUtils.sin(timeAccumulator * speed) * amplitude;
        chromaticAberrationEffect.setMaxDistortion(MathUtils.clamp(currentShift, 0.4f, 1.2f));
    }

    public CustomVfxEffectPooledFactoryImpl() {
        if (Application.ApplicationType.WebGL == Gdx.app.getType()) {
            gaussianBlurEffect = new GaussianBlurEffect(GaussianBlurEffect.BlurType.Gaussian3x3b);
            fisheyeEffect = new FisheyeEffect();
            oldTvEffect = new OldTvEffect();
            chromaticAberrationEffect = new ChromaticAberrationEffect(5);
            hellChromaticAberrationEffect = new ChromaticAberrationEffect(2);
            bloomEffect = new BloomEffect(new BloomEffect.Settings(
                GaussianBlurEffect.BlurType.Gaussian3x3b, // Переключаем с тяжелого 5x5 на легкий 3x3
                1,       // Снижаем blurPasses с 4 до 1! В WebGL один проход сэкономит вам сразу 6-8 переключений шейдеров
                0f,
                0.85f,
                1f,
                0.85f,
                1.1f,
                0.85f
            ));
            nfaaEffect = new NfaaEffect(true);
            crtEffect = new CrtEffect();
            lensFlareEffect = new LensFlareEffect();
            fxaaEffect = new FxaaEffect();
            waterDistortionEffect = new WaterDistortionEffect(4f, 12f);
            vignettingEffect = new VignettingEffect(false);
            smoothVignetteEffect = new SmoothVignetteEffect();
            gammaThresholdEffect = new GammaThresholdEffect(GammaThresholdEffect.Type.RGBA);
            levelsEffect = new LevelsEffect();
            radialBlurEffect = new RadialBlurEffect(2);
            filmGrainEffect = new FilmGrainEffect();
            radialDistortionEffect = new RadialDistortionEffect();
        } else {
            gaussianBlurEffect = new GaussianBlurEffect();
            fisheyeEffect = new FisheyeEffect();
            oldTvEffect = new OldTvEffect();
            chromaticAberrationEffect = new ChromaticAberrationEffect(10);
            hellChromaticAberrationEffect = new ChromaticAberrationEffect(2);
            bloomEffect = new BloomEffect(new BloomEffect.Settings(
                GaussianBlurEffect.BlurType.Gaussian5x5b,
                4,  // blurPasses: Снижаем с 10 до 2. Для разрешения 540p этого более чем достаточно.
                0f,      // blurAmount
                0.85f,   // bloomThreshold
                1f,      // baseIntensity
                0.85f,   // baseSaturation
                1.1f,    // bloomIntensity
                0.85f    // bloomSaturation
            ));
            nfaaEffect = new NfaaEffect(true);
            crtEffect = new CrtEffect();
            lensFlareEffect = new LensFlareEffect();
            fxaaEffect = new FxaaEffect();
            waterDistortionEffect = new WaterDistortionEffect(12f, 12f);
            vignettingEffect = new VignettingEffect(false);
            smoothVignetteEffect = new SmoothVignetteEffect();
            gammaThresholdEffect = new GammaThresholdEffect(GammaThresholdEffect.Type.RGBA);
            levelsEffect = new LevelsEffect();
            radialBlurEffect = new RadialBlurEffect(2);
            filmGrainEffect = new FilmGrainEffect();
            radialDistortionEffect = new RadialDistortionEffect();
        }

        this.putEffects(Title.HELL_VFX, new Array<ChainVfxEffect>() {{
            if (Application.ApplicationType.WebGL != Gdx.app.getType()) {
                this.add(lensFlareEffect);
            }
            this.add(hellChromaticAberrationEffect);
            this.add(vignettingEffect);
            this.add(waterDistortionEffect);
            this.add(radialBlurEffect);
            if (Application.ApplicationType.WebGL != Gdx.app.getType()) {
                this.add(bloomEffect);
            }
        }});
        zoomEffect = new ZoomEffect();

        this.putEffects(Title.MAIN_VFX, new Array<ChainVfxEffect>() {{
            //this.add(chromaticAberrationEffect);
            //this.add(vignettingEffect);
        }});
        this.putEffects(Title.THINKING_VFX, new Array<ChainVfxEffect>() {{
            //this.add(radialBlurEffect);
            this.add(chromaticAberrationEffect);
            this.add(vignettingEffect);
            this.add(gaussianBlurEffect);
        }});
    }

    @Override
    public void update(float deltaTime, String effectTitle) {
        timeAccumulator += deltaTime;
        switch (effectTitle) {
            case Title.MAIN_VFX:
                updateMainVfx(deltaTime);
                break;
            case Title.THINKING_VFX:
                updateThinking(deltaTime);
                break;
            case Title.HELL_VFX:
                updateHell(deltaTime);
                break;
        }
    }

    public GaussianBlurEffect gaussianBlurEffect;
    public MotionBlurEffect motionBlurEffect;
    public FisheyeEffect fisheyeEffect;
    public OldTvEffect oldTvEffect;
    public ChromaticAberrationEffect chromaticAberrationEffect;
    public ChromaticAberrationEffect hellChromaticAberrationEffect;
    public BloomEffect bloomEffect;
    public NfaaEffect nfaaEffect;
    public CrtEffect crtEffect;
    public LensFlareEffect lensFlareEffect;
    public FxaaEffect fxaaEffect;
    public WaterDistortionEffect waterDistortionEffect;
    public VignettingEffect vignettingEffect;
    public SmoothVignetteEffect smoothVignetteEffect;
    public ZoomEffect zoomEffect;
    public GammaThresholdEffect gammaThresholdEffect;
    public LevelsEffect levelsEffect;
    public RadialBlurEffect radialBlurEffect;
    public FilmGrainEffect filmGrainEffect;
    public RadialDistortionEffect radialDistortionEffect;


}
