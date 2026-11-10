package com.github.br.syberian.jam26;

import com.github.br.libgdx.common.structure.GamePlaySettings;

public class GamePlaySettingsImpl implements GamePlaySettings {

    private boolean dialogueActive;

    public void setDialogueActive(boolean dialogueActive) {
        this.dialogueActive = dialogueActive;
    }

    public boolean isDialogueActive() {
        return dialogueActive;
    }

}
