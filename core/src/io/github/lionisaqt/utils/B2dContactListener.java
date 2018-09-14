package io.github.lionisaqt.utils;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

/** Listens for contact between bodies.
 * @author Ryan Shee */
public class B2dContactListener implements ContactListener {
    private TraumaManager manager;

    public B2dContactListener(TraumaManager manager) {
        this.manager = manager;
    }

    @Override
    public void beginContact(Contact contact) {
        /* Body variables in case I want to do something with body.isBullet() */
        Body aBody = contact.getFixtureA().getBody();
        Body bBody = contact.getFixtureB().getBody();

        /* EntityInfo variables for everything else */
        EntityInfo a = (EntityInfo)aBody.getUserData();
        EntityInfo b = (EntityInfo)bBody.getUserData();

        /* If two entities of opposing sides collide, deal damage to both of them */
        if ((a.friendly && !b.friendly) || (!a.friendly && b.friendly)) {
            a.hp -= b.dmg;
            b.hp -= a.dmg;

            if (a.isPlayer || b.isPlayer) { manager.addTrauma(a.isPlayer ? a.impact : b.impact); }
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
