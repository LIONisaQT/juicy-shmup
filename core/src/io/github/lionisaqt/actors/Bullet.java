package io.github.lionisaqt.actors;


import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Pool.Poolable;

import box2dLight.PointLight;
import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

import static io.github.lionisaqt.JuicyShmup.PPM;

/** Poolable bullet class. Can be used for both friendly and non-friendly units.
 * @author Ryan Shee */
public class Bullet extends SpaceEntity implements Poolable {
    private Sound pew;

    /** Constructs a new bullet.
     * @param game Reference to the game for assets
     * @param screen Reference for in-game stuff */
    public Bullet(JuicyShmup game, InGame screen) {
        super(game, screen);
        scale = 0.1f * PPM;
        info.maxHp = 1;
        info.hp = info.maxHp;
        info.dmg = 10;
        info.speed = 50;
        info.impact = 0.05f;
        info.isPlayer = false;
    }

    /** Initializes important values if they're null, called after getting a bullet from the pool.
     * @param x Initial x position
     * @param y Initial y position
     * @param friendly Whether bullet is friendly */
    void init(float x, float y, boolean friendly) {
        if (sprite == null) {
            sprite = new Sprite(game.assets.manager.get(game.assets.bullet));
            sprite.setScale(scale * 2);
        }

        if (body == null) {
            makeBody(x, y, "circle");
            body.setBullet(true);
            info.friendly = friendly;
            body.setUserData(info);
            body.setLinearVelocity(0, friendly ? info.speed : -info.speed);
        }

        if (color == null)
            color = new Color(friendly ? 0 : 1, friendly ? 1 : 0, 0, 1);
        else
            color.set(friendly ? 0 : 1, friendly ? 1 : 0, 0, 1);

        if (light == null) {
            light = new PointLight(screen.eManager.rayHandler, 128, color, 50 * PPM, body.getPosition().x, body.getPosition().y);
            light.setStaticLight(false);
            light.setSoft(true);
            light.attachToBody(body);
        }

        deathSound = info.friendly ? game.assets.manager.get(game.assets.hit1) : game.assets.manager.get(game.assets.hurt1);

        if (pew == null) pew = game.assets.manager.get(game.assets.shoot);
        pew.play(0.25f);
    }

    @Override
    public void update(float deltaTime) {
        if (info.hp <= 0) {
            die();
            return;
        }

        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);

        // I think tracers make clutters the visuals too much
        /*ParticleEffectPool.PooledEffect p = info.friendly ? screen.eManager.tracersPool.obtain() : screen.eManager.enemyTracersPool.obtain();
        p.setPosition(body.getPosition().x, body.getPosition().y + 1);
        p.scaleEffect(scale * 2.5f);
        p.start();
        screen.eManager.effects.add(p);*/

        /* Destroys bullet if offscreen */
        if (body.getPosition().y + sprite.getHeight() * sprite.getScaleY() / 2 > JuicyShmup.GAME_HEIGHT * PPM || body.getPosition().y + sprite.getHeight() * sprite.getScaleY() / 2 < 0) free();
}

    @Override
    public void die() {
        super.die();
        deathSound.play();
        free();
    }

    /** Removes bullet from the active array of bullets and frees it from the pool. Sends body to
     * the array of dead bodies for processing. */
    public void free() {
        screen.bullets.removeValue(this, false);
        screen.bulletPool.free(this);
    }

    @Override
    public void reset() {
        light.remove(true);
        light = null;
        color = null;
        body = null;
        sprite = null;
        pew = null;
        info.hp = info.maxHp;
    }
}
