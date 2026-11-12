package com.github.br.libgdx.common.structure.screen.ui;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.utils.viewport.Viewport;

public interface CustomLayerRenderer {

    void render(MapLayer layer, Viewport viewport);

    void resize(Viewport viewport, int width, int height);

}
