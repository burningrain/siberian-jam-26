package com.github.br.syberian.jam26.ecs.systems.cutscene.step;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.github.br.syberian.jam26.EntityFactory;
import com.github.br.syberian.jam26.ecs.components.FollowTheTargetComponent;
import com.github.br.syberian.jam26.ecs.components.RenderComponent;
import com.github.br.syberian.jam26.ecs.systems.FollowTheTargetSystem;
import com.github.br.syberian.jam26.ecs.systems.cutscene.CutsceneStep;
import com.github.tommyettinger.textra.TypingLabel;

public class SayStep implements CutsceneStep {
    private final int targetEntityId;
    private final String text;

    private final Sound sound;
    private int labelEntityId = -1;

    public SayStep(int targetEntityId, String text, Sound sound) {
        this.targetEntityId = targetEntityId;
        this.text = text;
        this.sound = sound;
    }

    @Override
    public void start(World world) {
        // Создаем облачко диалога (высота 32f как в вашем коде)
        EntityFactory entityFactory = world.getSystem(EntityFactory.class);
        labelEntityId = entityFactory.createTalkingLabel(targetEntityId, "talking_layer", 8f);

        RenderComponent renderComponent = world.getMapper(RenderComponent.class).get(labelEntityId);
        Container<TypingLabel> labelContainer = (Container<TypingLabel>) renderComponent.actor;
        TypingLabel typingLabel = labelContainer.getActor();

        typingLabel.setText(text);
        typingLabel.invalidateHierarchy();
        labelContainer.invalidate();
        labelContainer.pack();

        // Позиционируем ровно над головой
        FollowTheTargetComponent follow = world.getMapper(FollowTheTargetComponent.class).get(labelEntityId);
        if (follow != null) {
            follow.offsetY = 32f + labelContainer.getHeight() / 5f;
            follow.offsetX = -labelContainer.getWidth() / 2f;
        }

        sound.play();
    }

    @Override
    public boolean update(World world, float delta) {
        // Шаг завершается, когда игрок нажимает клавишу E
        return Gdx.input.isKeyJustPressed(Input.Keys.E);
    }

    @Override
    public void end(World world) {
        // Удаляем облачко, когда реплика завершена (Box2dRenderSystem сам удалит Actor из Stage!)
        if (labelEntityId != -1) {
            world.delete(labelEntityId);
        }
    }
}
