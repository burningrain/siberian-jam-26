package com.github.br.libgdx.common.structure.screen.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;

public class ActorUtils {

    /**
     * Отражает актора по горизонтали относительно его центра.
     */
    public static void flipHorizontal(Actor actor, boolean isFlipX) {
        // Устанавливаем точку origin в центр актора для корректного поворота/масштабирования
        actor.setOrigin(actor.getWidth() / 2f, actor.getHeight() / 2f);

        // Если flip == true, ставим масштаб -1, иначе возвращаем исходный 1
        float scaleX = isFlipX ? -1f : 1f;
        actor.setScaleX(scaleX);
    }

    /**
     * Отражает актора по вертикали относительно его центра.
     */
    public static void flipVertical(Actor actor, boolean isFlipY) {
        actor.setOrigin(actor.getWidth() / 2f, actor.getHeight() / 2f);

        float scaleY = isFlipY ? -1f : 1f;
        actor.setScaleY(scaleY);
    }

}
