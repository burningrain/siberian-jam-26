package com.github.br.syberian.jam26.ecs.systems.physics;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.github.br.syberian.jam26.ecs.components.TriggerContactComponent;
import com.github.br.syberian.jam26.ecs.systems.physics.contact.ContactData;
import com.github.br.syberian.jam26.ecs.systems.physics.contact.ContactDataImpl;
import com.github.br.syberian.jam26.ecs.systems.physics.contact.PlayerContactDataImpl;

public class TriggerContactResolver implements PhysicsContactResolver<TriggerContactDataImpl> {

    private final World ecsWorld;

    public TriggerContactResolver(World ecsWorld) {
        this.ecsWorld = ecsWorld;
    }

    @Override
    public void beginContact(Contact contact, TriggerContactDataImpl myContact, Body myBody, Body object, ContactData objectContact) {
        handleTriggerContact(contact, myContact, objectContact, true);
    }

    @Override
    public void endContact(Contact contact, TriggerContactDataImpl myContact, Body myBody, Body object, ContactData objectContact) {
        handleTriggerContact(contact, myContact, objectContact, false);
    }

    private void handleTriggerContact(Contact contact, TriggerContactDataImpl triggerData, ContactData objectData, boolean isBegin) {
        if (objectData == null || objectData.isDeleted()) return;

        // Получаем фикстуру объекта, который наступил на триггер
        // Нам нужно определить, какая из фикстур принадлежит триггеру, а какая — объекту
        Fixture objectFixture =
            contact.getFixtureA().getBody().getUserData() == objectData ?
                contact.getFixtureA() : contact.getFixtureB();

        // Проверяем, что коллизия произошла именно с сенсором игрока "PLAYER_SENSOR"
        if (objectFixture.getUserData() instanceof PlayerContactDataImpl) {
            // Безопасно вытаскиваем entityId игрока (предполагаем, что ContactDataImpl хранит его)
            int playerId = objectData.getEntityId();
            int triggerId = triggerData.getEntityId();

            ComponentMapper<TriggerContactComponent> mContact = ecsWorld.getMapper(TriggerContactComponent.class);

            if (isBegin) {
                // Вешаем компонент контакта на триггер
                TriggerContactComponent contactComp = mContact.create(triggerId);
                contactComp.activatorEntityId = playerId;
            } else {
                // Удаляем компонент контакта, игрок вышел
                mContact.remove(triggerId);
            }

        }
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold, TriggerContactDataImpl myContact, Body myBody, Body object, ContactData objectContact) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse, TriggerContactDataImpl myContact, Body myBody, Body object, ContactData objectContact) {
    }
}
