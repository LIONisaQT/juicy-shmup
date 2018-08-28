package io.github.lionisaqt.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool.Poolable;

import box2dLight.PointLight;
import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

import static io.github.lionisaqt.JuicyShmup.PPM;

/** Poolable bullet class. Can be used for both friendly and non-friendly units.
 * @author Ryan Shee */
public class Bullet extends SpaceEntity implements Poolable {
    /* Reference to the game for assets */
    private JuicyShmup game;

    /* Light emitted by this bullet */
    private PointLight bLight;

    /* Light color to help tell friendly from non-friendly */
    private Color bColor;

    /** Constructs a new bullet.
     * @param game Reference to the game for assets
     * @param screen Reference for in-game stuff */
    public Bullet(JuicyShmup game, InGame screen) {
        super(screen);
        this.game = game;
        info.maxHp = 1;
        info.hp = info.maxHp;
        info.dmg = 10;
        info.speed = 25;
        info.impact = 0.1f;
        info.isPlayer = false;
    }

    /** Initializes important values if they're null, called after getting a bullet from the pool.
     * @param x Initial x position
     * @param y Initial y position
     * @param friendly Whether bullet is friendly */
    public void init(float x, float y, boolean friendly) {
        if (sprite == null) {
            sprite = new Sprite(game.assets.manager.get(game.assets.bullet));
            sprite.setScale(0.1f * PPM);
        }

        if (body == null) {
            makeBody(x, y, "circle");
            body.setBullet(true);
            info.friendly = friendly;
            body.setUserData(info);
            body.setLinearVelocity(0, friendly ? info.speed : -info.speed);
        }

        if (bColor == null)
            bColor = new Color(friendly ? 0 : 1, friendly ? 1 : 0, 0, 1);
        else
            bColor.set(friendly ? 0 : 1, friendly ? 1 : 0, 0, 1);

        if (bLight == null) {
            bLight = new PointLight(screen.rayHandler, 128, bColor, 50 * PPM, body.getPosition().x, body.getPosition().y);
            bLight.setStaticLight(false);
            bLight.setSoft(true);
            bLight.attachToBody(body);
        }

        // TODO: Play sound here
    }

    @Override
    public void update(float deltaTime) {
        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);

        /* Kills bullet and snuffs its light */
        if (info.hp <= 0 || body.getPosition().y + sprite.getHeight() * sprite.getScaleY() / 2 > JuicyShmup.GAME_HEIGHT  * PPM) {
            bLight.setActive(false);
            free();
        }
    }

    /** Removes bullet from the active array of bullets and frees it from the pool. */
    public void free() {
        screen.bullets.removeValue(this, false);
        screen.bulletPool.free(this);
    }

    @Override
    public void reset() {
        /* Destroys the body, otherwise physics would still be calculating for this object */
        for (Fixture f : body.getFixtureList()) body.destroyFixture(f);

        body = null;
        sprite = null;
        bColor = null;
        bLight = null;
        info.hp = info.maxHp;
    }

    /** Disposes all disposable values. */
    public void dispose() {
        super.dispose();
        bLight.dispose();
    }
}
