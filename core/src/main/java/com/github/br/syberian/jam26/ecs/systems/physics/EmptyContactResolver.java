package com.github.br.syberian.jam26.ecs.systems.physics;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.github.br.syberian.jam26.ecs.systems.physics.contact.ContactData;

public class EmptyContactResolver implements PhysicsContactResolver<ContactData> {
    @Override
    public void beginContact(Contact contact, ContactData myContact, Body myBody, Body object, ContactData objectContact) {

    }

    @Override
    public void endContact(Contact contact, ContactData myContact, Body myBody, Body object, ContactData objectContact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold, ContactData myContact, Body myBody, Body object, ContactData objectContact) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse, ContactData myContact, Body myBody, Body object, ContactData objectContact) {

    }
}
