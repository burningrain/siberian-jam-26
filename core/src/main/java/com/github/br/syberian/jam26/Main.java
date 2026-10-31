package com.github.br.syberian.jam26;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.ParticleEffectLoader;
import com.badlogic.gdx.assets.loaders.SoundLoader;
import com.badlogic.gdx.assets.loaders.TextureAtlasLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.br.libgdx.common.structure.AbstractSimpleGame;
import com.github.br.libgdx.common.structure.GameSettings;
import com.github.br.libgdx.common.structure.PlatformConfigurator;
import com.github.br.libgdx.common.structure.screen.statemachine.GameScreenState;
import com.github.br.syberian.jam26.screens.GameScreens;
import com.github.tommyettinger.textra.FWSkinLoader;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends AbstractSimpleGame<UserFactoryImpl> {

    public Main() {
        super(null);
    }

    public Main(PlatformConfigurator platformConfigurator) {
        super(platformConfigurator);
    }

    @Override
    protected UserFactoryImpl createUserFactory() {
        return new UserFactoryImpl();
    }

    @Override
    protected GameScreenState createStartState() {
        return GameScreens.TEST_ROOM;
    }

    @Override
    protected void initLoaders(AssetManager assetManager, FileHandleResolver fileHandleResolver) {
        // графика
        assetManager.setLoader(Texture.class, new TextureLoader(fileHandleResolver));
        assetManager.setLoader(TextureAtlas.class, new TextureAtlasLoader(fileHandleResolver));
        //assetManager.setLoader(Skin.class, new FreeTypeSkinLoader(fileHandleResolver));

        // лоадер для FWSkin
        assetManager.setLoader(Skin.class, new FWSkinLoader(fileHandleResolver));

        // эффекты частиц
        assetManager.setLoader(ParticleEffect.class, ".p", new ParticleEffectLoader(fileHandleResolver));

        // звук
        assetManager.setLoader(Sound.class, new SoundLoader(fileHandleResolver));
        assetManager.setLoader(Music.class, new MusicLoader(fileHandleResolver));

        // карты редакторов уровней
        //assetManager.setLoader(TiledMap.class, new TmxMapLoader(fileHandleResolver));

        // шрифты
        assetManager.setLoader(BitmapFont.class, new FreetypeFontLoader(fileHandleResolver));
    }

    @Override
    protected void fillGameSettings(GameSettings.Builder builder) {
        builder.setCenterCamera(false);

        builder.setVirtualScreenWidth(Constants.VIRTUAL_WIDTH);
        builder.setVirtualScreenHeight(Constants.VIRTUAL_HEIGHT);

        builder.setUseKtx2(Constants.IS_USE_KTX2);
    }

}
