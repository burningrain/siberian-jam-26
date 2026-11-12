package com.github.br.syberian.jam26.ecs.systems.physics.contact;

public class PlayerContactDataImpl implements ContactData {

    public int entityId;
    public boolean isDeleted;

    public PlayerContactDataImpl(int entityId) {
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

    @Override
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

}
