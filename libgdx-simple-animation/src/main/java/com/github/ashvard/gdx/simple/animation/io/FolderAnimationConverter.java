package com.github.ashvard.gdx.simple.animation.io;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.github.ashvard.gdx.simple.animation.io.dto.AnimationDto;
import com.github.ashvard.gdx.simple.animation.SimpleAnimation;

public class FolderAnimationConverter {

    private final JsonConverter jsonConverter = new JsonConverter();
    private final AnimationConverter animationConverter = new AnimationConverter();

    public SimpleAnimation from(FolderAnimDto anim) {
        AnimationDto animationDto = jsonConverter.from(anim.getFsm());
        TextureAtlas textureAtlas = anim.getTextureAtlas();
        return animationConverter.from(animationDto, textureAtlas);
    }

}
