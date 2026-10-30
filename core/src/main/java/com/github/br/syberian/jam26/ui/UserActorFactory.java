package com.github.br.syberian.jam26.ui;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.br.libgdx.common.structure.screen.ui.ActorFactory;
import com.github.br.libgdx.common.structure.screen.ui.DefaultActorFactory;

public class UserActorFactory implements ActorFactory {


    private final DefaultActorFactory delegate;

    private final Skin skin;
    private final AssetManager assetManager;

    public UserActorFactory(Skin skin, AssetManager assetManager) {
        this.delegate = new DefaultActorFactory(skin, assetManager);
        this.skin = skin;
        this.assetManager = assetManager;
    }


    @Override
    public Actor getActor(MapObject object) {
        String name = object.getName();
        MapProperties properties = object.getProperties();

        String actorType = properties.get(DefaultActorFactory.ACTOR_TYPE, String.class);
        switch (name) {

        }

        return delegate.getActor(object);
    }



}
