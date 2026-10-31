package com.github.br.syberian.jam26;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.syberian.jam26.ecs.components.TriggerAction;

public class TriggerActionFactory extends BaseSystem {

    @Override
    protected void processSystem() {
    }

    public TriggerAction create(TriggerType triggerType) {
        switch (triggerType) {
            case SHOW_MESSAGE:
                return createShowMessageAction();
            case PICK_UP_ITEM:
                return createPickUpItemAction();

            default:
                throw new GdxRuntimeException("TriggerActionFactory#create: [" +
                    triggerType + "] is not implemented yet");
        }
    }

    private TriggerAction createShowMessageAction() {
        return new TriggerAction() {
            @Override
            public void execute(World world, int triggerEntityId, int activatorEntityId) {
                System.out.println("ВОШЕЛ В ОБЛАСТЬ ТРИГГЕРА");
            }
        };
    }

    private TriggerAction createPickUpItemAction() {
        return new TriggerAction() {
            @Override
            public void execute(World world, int triggerEntityId, int activatorEntityId) {
                //TODO физически удалить предмет, добавить его в инвентарь героя
            }
        };
    }

}
