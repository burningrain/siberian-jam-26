package com.github.br.syberian.jam26.ecs.systems;

import com.artemis.BaseSystem;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.br.libgdx.common.structure.GameManager;

public class UtilsObjectsHolder extends BaseSystem {

    private Viewport viewport;
    private GameManager gameManager;

    public UtilsObjectsHolder() {
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    protected void initialize() {
        setEnabled(false);  // Отключаем ежекадровое обновление
    }

    @Override
    protected void processSystem() {
    }

}
