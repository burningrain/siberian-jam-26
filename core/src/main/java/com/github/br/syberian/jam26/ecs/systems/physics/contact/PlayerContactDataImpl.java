package com.github.br.syberian.jam26.ecs.systems.physics.contact;

public class PlayerContactDataImpl implements ContactData {

    public int entityId;
    public boolean isDeleted;

    public PlayerContactDataImpl(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public int getEntityId() {
        return 0;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

}
