package com.github.br.syberian.jam26;

import com.badlogic.gdx.maps.MapObject;

public class EntityCreationData {

    public ActorTypeEnum actorType;
    public String tag;
    public String layerName;
    public MapObject mapObject;
    public float x;
    public float y;
    public float width;
    public float height;
    public float originX;
    public float originY;
    public float rotation;

    public EntityCreationData(
        ActorTypeEnum actorType,
        String tag,
        String layerName,
        MapObject mapObject,
        float x,
        float y,
        float width,
        float height,
        float originX,
        float originY,
        float rotation
    ) {
        this.actorType = actorType;
        this.tag = tag;
        this.layerName = layerName;
        this.mapObject = mapObject;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.originX = originX;
        this.originY = originY;
        this.rotation = rotation;
    }

}
