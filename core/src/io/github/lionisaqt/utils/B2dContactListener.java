package io.github.lionisaqt.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;

public class B2dContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fA = contact.getFixtureA();
        Fixture fB = contact.getFixtureB();
        EntityInfo eA, eB;

        if (fA == null || fB == null) return;
        eA = (EntityInfo)fA.getBody().getUserData();
        eB = (EntityInfo)fB.getBody().getUserData();

        if ((eA.friendly && !eB.friendly) || (!eA.friendly && eB.friendly)) {
            eA.hp -= eB.dmg;
            eB.hp -= eA.dmg;
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
