package com.github.br.syberian.jam26.ecs.systems.physics.contact;

public class TriggerContactDataImpl implements ContactData {

    public int entityId;
    public boolean isDeleted;

    public TriggerContactDataImpl(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

}
