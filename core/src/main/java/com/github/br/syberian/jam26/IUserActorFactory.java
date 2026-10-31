package com.github.br.syberian.jam26;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.github.br.libgdx.common.structure.screen.ui.ActorFactory;

public interface IUserActorFactory extends ActorFactory {

    Actor getCustomUiActor(ActorTypeEnum actorType);

    void setEntityFactory(EntityFactory entityFactory);

}
