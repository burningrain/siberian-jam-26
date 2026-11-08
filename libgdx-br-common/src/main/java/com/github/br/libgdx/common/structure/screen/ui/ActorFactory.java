package com.github.br.libgdx.common.structure.screen.ui;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.scenes.scene2d.Actor;

public interface ActorFactory {

    Actor createActor(String layerName, MapObject object);

}
