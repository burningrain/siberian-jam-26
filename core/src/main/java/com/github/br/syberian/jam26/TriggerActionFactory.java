package com.github.br.syberian.jam26;

import com.artemis.BaseSystem;
import com.artemis.World;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.github.br.syberian.jam26.ecs.components.TriggerAction;
import com.github.br.libgdx.common.structure.screen.RestartScreen;

public class TriggerActionFactory extends BaseSystem {

    private final RestartScreen currentScreen;

    public TriggerActionFactory(RestartScreen currentScreen) {
        this.currentScreen = currentScreen;
    }

    @Override
    protected void processSystem() {
    }

    public TriggerAction create(TriggerType triggerType) {
        switch (triggerType) {
            case RESTART_LEVEL:
                return createRestartLevelAction();
            case SHOW_MESSAGE:
                return createShowMessageAction();
            case PICK_UP_ITEM:
                return createPickUpItemAction();
            case EXIT_TRIGGER:
                return createTriggerExitAction();

            default:
                throw new GdxRuntimeException("TriggerActionFactory#create: [" +
                    triggerType + "] is not implemented yet");
        }
    }

    private TriggerAction createTriggerExitAction() {
        return new TriggerAction() {
            @Override
            public void execute(World world, int triggerEntityId, int activatorEntityId) {
                currentScreen.triggerExit();
            }
        };
    }

    private TriggerAction createRestartLevelAction() {
        return new TriggerAction() {
            @Override
            public void execute(World world, int triggerEntityId, int activatorEntityId) {
                currentScreen.restart();
            }
        };
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
