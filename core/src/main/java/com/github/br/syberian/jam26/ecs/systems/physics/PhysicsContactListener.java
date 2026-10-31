package com.github.br.syberian.jam26.ecs.systems.physics;

import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.github.br.syberian.jam26.ecs.components.DeleteIntentComponent;
import com.github.br.syberian.jam26.ecs.components.PhysicsComponent;
import com.github.br.syberian.jam26.ecs.components.PlayerComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerComponent;
import com.github.br.syberian.jam26.ecs.components.TriggerContactComponent;
import com.github.br.syberian.jam26.ecs.systems.physics.contact.ContactData;

public class PhysicsContactListener extends BaseSystem implements ContactListener {

    protected ComponentMapper<DeleteIntentComponent> mDelete;

    protected ComponentMapper<PlayerComponent> mPlayer;
    protected ComponentMapper<PhysicsComponent> mPhysics;

    protected ComponentMapper<TriggerComponent> mTrigger;
    protected ComponentMapper<TriggerContactComponent> mContact;

    private TriggerContactResolver triggerContactResolver;


    // ресолверы контактов
    private EmptyContactResolver emptyContactResolver = new EmptyContactResolver();

    //TODO добавить ресолверы

    // ресолверы контактов

    @Override
    protected void processSystem() {
    }

    @Override
    protected void initialize() {
        setEnabled(false);

        this.mPlayer = getWorld().getMapper(PlayerComponent.class);
        this.mPhysics = getWorld().getMapper(PhysicsComponent.class);
        this.mDelete = getWorld().getMapper(DeleteIntentComponent.class);

        this.mTrigger = getWorld().getMapper(TriggerComponent.class);
        this.mContact = getWorld().getMapper(TriggerContactComponent.class);

        this.triggerContactResolver = new TriggerContactResolver(getWorld());
    }

    public PhysicsContactResolver<? extends ContactData> getPhysicsContact(ContactData data) {
        if (data != null && data.isDeleted()) {
            return emptyContactResolver;
        }

        // 3. Добавляем проверку типа данных контакта для триггера
        if (data instanceof TriggerContactDataImpl) {
            return triggerContactResolver;
        }

        return emptyContactResolver;
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Body bodyA = fixtureA.getBody();
        Body bodyB = fixtureB.getBody();

        ContactData contactDataA = (ContactData) bodyA.getUserData();
        ContactData contactDataB = (ContactData) bodyB.getUserData();

        @SuppressWarnings("unchecked")
        PhysicsContactResolver resolverBodyContactA = getPhysicsContact(contactDataA);
        resolverBodyContactA.beginContact(contact, contactDataA, bodyA, bodyB, contactDataB);

        @SuppressWarnings("unchecked")
        PhysicsContactResolver resolverBodyContactB = getPhysicsContact(contactDataB);
        resolverBodyContactB.beginContact(contact, contactDataB, bodyB, bodyA, contactDataA);
    }

    @Override
    public void endContact(Contact contact) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Body bodyA = fixtureA.getBody();
        Body bodyB = fixtureB.getBody();

        ContactData contactDataA = (ContactData) bodyA.getUserData();
        ContactData contactDataB = (ContactData) bodyB.getUserData();

        @SuppressWarnings("unchecked")
        PhysicsContactResolver resolverBodyContactA = getPhysicsContact(contactDataA);
        resolverBodyContactA.endContact(contact, contactDataA, bodyA, bodyB, contactDataB);

        @SuppressWarnings("unchecked")
        PhysicsContactResolver resolverBodyContactB = getPhysicsContact(contactDataB);
        resolverBodyContactB.endContact(contact, contactDataB, bodyB, bodyA, contactDataA);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Body bodyA = fixtureA.getBody();
        Body bodyB = fixtureB.getBody();

        ContactData contactDataA = (ContactData) bodyA.getUserData();
        ContactData contactDataB = (ContactData) bodyB.getUserData();

        @SuppressWarnings("unchecked")
        PhysicsContactResolver resolverBodyContactA = getPhysicsContact(contactDataA);
        resolverBodyContactA.preSolve(contact, oldManifold, contactDataA, bodyA, bodyB, contactDataB);

        @SuppressWarnings("unchecked")
        PhysicsContactResolver resolverBodyContactB = getPhysicsContact(contactDataB);
        resolverBodyContactB.preSolve(contact, oldManifold, contactDataB, bodyB, bodyA, contactDataA);
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        Fixture fixtureA = contact.getFixtureA();
        Fixture fixtureB = contact.getFixtureB();

        Body bodyA = fixtureA.getBody();
        Body bodyB = fixtureB.getBody();

        ContactData contactDataA = (ContactData) bodyA.getUserData();
        ContactData contactDataB = (ContactData) bodyB.getUserData();

        @SuppressWarnings("unchecked")
        PhysicsContactResolver resolverBodyContactA = getPhysicsContact(contactDataA);
        resolverBodyContactA.postSolve(contact, impulse, contactDataA, bodyA, bodyB, contactDataB);

        @SuppressWarnings("unchecked")
        PhysicsContactResolver resolverBodyContactB = getPhysicsContact(contactDataB);
        resolverBodyContactB.postSolve(contact, impulse, contactDataB, bodyB, bodyA, contactDataA);
    }

}
