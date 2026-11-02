package com.github.br.syberian.jam26.ecs.systems;

import com.artemis.BaseSystem;
import com.badlogic.gdx.utils.viewport.Viewport;

public class UtilsObjectsHolder extends BaseSystem {

    private Viewport viewport;

    public UtilsObjectsHolder() {
    }

    public Viewport getViewport() {
        return viewport;
    }

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    @Override
    protected void initialize() {
        setEnabled(false);  // Отключаем ежекадровое обновление
    }

    @Override
    protected void processSystem() {
    }

}
