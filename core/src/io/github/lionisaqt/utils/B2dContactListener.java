package io.github.lionisaqt.utils;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

/** Listens for contact between bodies.
 * @author Ryan Shee */
public class B2dContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        EntityInfo a = (EntityInfo)contact.getFixtureA().getBody().getUserData();
        EntityInfo b = (EntityInfo)contact.getFixtureB().getBody().getUserData();

        /* If two entities of opposing sides collide, deal damage to both of them */
        if ((a.friendly && !b.friendly) || (!a.friendly && b.friendly)) {
            a.hp -= b.dmg;
            b.hp -= a.dmg;
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
