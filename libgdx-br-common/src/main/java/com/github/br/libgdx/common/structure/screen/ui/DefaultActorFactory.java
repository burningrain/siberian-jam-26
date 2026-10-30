package com.github.br.libgdx.common.structure.screen.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.github.tommyettinger.textra.TypingLabel;

public class DefaultActorFactory implements ActorFactory {

    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";

    public interface ActorType {
        String IMAGE = "image";
        String ANIMATED_IMAGE = "animated_image";
        String LABEL = "label";
        String TYPING_LABEL = "typing_label";
        String IMAGE_BUTTON = "image_button";
        String IMAGE_TEXT_BUTTON = "image_text_button";
    }

    public static final String ACTOR_TYPE = "actor_type";
    public static final String ATLAS_NAME = "atlas_name";
    public static final String REGION_NAME = "region_name";
    public static final String PLAY_MODE = "play_mode";
    public static final String TEXT = "text";
    public static final String STYLE_NAME = "style_name";
    public static final String FRAME_DURATION = "frame_duration";

    private final Skin skin;
    private final AssetManager assetManager;

    public DefaultActorFactory(Skin skin, AssetManager assetManager) {
        this.skin = skin;
        this.assetManager = assetManager;
    }

    @Override
    public Actor getActor(MapObject object) {
        String name = object.getName();
        MapProperties properties = object.getProperties();

        String actorType = properties.get(ACTOR_TYPE, String.class);

        switch (actorType) {
            case ActorType.IMAGE:
                return createImage(
                    properties.get(ATLAS_NAME, String.class),
                    properties.get(REGION_NAME, String.class)
                );
            case ActorType.ANIMATED_IMAGE:
                return createAnimatedImage(
                    properties.get(ATLAS_NAME, String.class),
                    properties.get(REGION_NAME, String.class),
                    Animation.PlayMode.valueOf(properties.get(PLAY_MODE, String.class)),
                    properties.get(FRAME_DURATION, Float.class)
                );

            case ActorType.LABEL:
                return createLabel(
                    properties.get(TEXT, String.class),
                    properties.get(STYLE_NAME, String.class),
                    properties.get(WIDTH, float.class),
                    properties.get(HEIGHT, float.class)
                );
            case ActorType.TYPING_LABEL:
                return createTypingLabel(
                    properties.get(TEXT, String.class),
                    properties.get(STYLE_NAME, String.class),
                    properties.get(WIDTH, float.class),
                    properties.get(HEIGHT, float.class)
                );

            case ActorType.IMAGE_BUTTON:
                return createImageButton((Boolean) properties.get("flip"));
            case ActorType.IMAGE_TEXT_BUTTON:
                return createImageTextButton(properties.get(TEXT, String.class));
            default:
                throw new IllegalArgumentException("unknown stage2d actor type [" + actorType + "]. actor name: " + name);
        }
    }

    public Image createImage(String atlasName, String regionName) {
        TextureAtlas textureAtlas = assetManager.get(atlasName, TextureAtlas.class);
        TextureAtlas.AtlasRegion region = textureAtlas.findRegion(regionName);

        return new Image(region);
    }

    public Label createLabel(String buttonText, String styleName, float width, float height) {
        //TODO сам текст нужно брать из файлика локализации !!!
        Label label = new Label(buttonText, skin, styleName);
        label.setAlignment(Align.topLeft);
        label.setWidth(width);
        label.setHeight(height);

        label.setWrap(true);

        return label;
    }

    public TypingLabel createTypingLabel(String buttonText, String styleName, float width, float height) {
        //TODO сам текст нужно брать из файлика локализации !!!
        TypingLabel label = new TypingLabel(buttonText, skin, styleName);
        label.setAlignment(Align.topLeft);
        label.setWidth(width);
        label.setHeight(height);

        label.setWrap(true);

        return label;
    }

    public AnimatedImage createAnimatedImage(String atlasName, String regionName, Animation.PlayMode playMode, float frameDuration) {
        TextureAtlas textureAtlas = assetManager.get(atlasName, TextureAtlas.class);
        Array<TextureAtlas.AtlasRegion> regions = textureAtlas.findRegions(regionName);
        Animation<TextureRegion> animation = new Animation<>(
            frameDuration, regions, playMode
        );

        return new AnimatedImage(animation);
    }

    public ImageTextButton createImageTextButton(String buttonText) {
        //TODO сам текст нужно брать из файлика локализации !!!
        return new ImageTextButton(buttonText, skin);
    }

    public ImageButton createImageButton(Boolean isFlip) {
        ImageButton button = new ImageButton(skin);
        if (isFlip != null && isFlip) {
            button.setTransform(true); // Разрешаем трансформацию
            button.setScale(-1, 1);    // Отражаем по горизонтали
            button.setOrigin(Align.center); // Устанавливаем центр для отражения

            // Корректировка хитбокса (важно для нажатий!)
            // Из-за scale(-1) координаты нажатий будут неправильными, если не скорректировать
        }

        return button;
    }

}
