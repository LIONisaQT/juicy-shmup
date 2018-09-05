package io.github.lionisaqt.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.Pool.Poolable;

import java.util.Random;

import box2dLight.PointLight;
import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

import static io.github.lionisaqt.JuicyShmup.PPM;

/** Poolable generic enemy class.
 * @author Ryan Shee */
public class Enemy extends SpaceEntity implements Poolable {
    /* Reference to the game for assets */
    private JuicyShmup game;

    /** Constructs a new enemy.
     * @param game Reference to the game for assets
     * @param screen Reference for in-game stuff */
    public Enemy(JuicyShmup game, InGame screen) {
        super(screen);
        this.game = game;
        scale = 0.25f * PPM;
        info.maxHp = 100;
        info.hp = info.maxHp;
        info.dmg = 100;
        info.speed = -2;
        info.impact = 1f;
        info.friendly = false;
        info.isPlayer = false;
    }

    /** Initializes important values if they're null, called after getting an enemy from the pool. */
    public void init() {
        if (sprite == null) {
            sprite = new Sprite(game.assets.manager.get(game.assets.img));
            sprite.setScale(scale);
        }

        if (body == null) {
            makeBody(new Random().nextFloat() * JuicyShmup.GAME_WIDTH * PPM - sprite.getWidth() * sprite.getScaleX() / 2, JuicyShmup.GAME_HEIGHT * PPM - sprite.getHeight() * sprite.getScaleY() / 2, "square");
            body.setUserData(info);
            body.setLinearVelocity(0, info.speed);
            sprite.setPosition(body.getPosition().x, body.getPosition().y);
        }

        if (color == null)
            color = new Color(info.friendly ? 0 : 1, info.friendly ? 1 : 0, 0, 1);
        else
            color.set(info.friendly ? 0 : 1, info.friendly ? 1 : 0, 0, 1);

        /* Engine light */
        if (light == null) {
            light = new PointLight(screen.rayHandler, 128, color, 100 * PPM, body.getPosition().x, body.getPosition().y);
            light.setStaticLight(false);
            light.setSoft(true);
            light.setPosition(body.getPosition().x, body.getPosition().y + 1.1f);
        }
    }

    @Override
    public void update(float deltaTime) {
        if (info.hp <= 0) {
            die(deltaTime);
            return;
        }

        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
        light.setPosition(body.getPosition().x, body.getPosition().y + 1);

        /* Engine particle effects! */
        PooledEffect p = screen.enemyEnginePool.obtain();
        p.setPosition(body.getPosition().x, body.getPosition().y + 1);
        p.scaleEffect(scale);
        p.start();
        screen.effects.add(p);

        /* Checks to make sure body speed is constant */
        if (body.getLinearVelocity().y != info.speed)
            body.setLinearVelocity(body.getLinearVelocity().x, info.speed);

        /* Kills enemy if offscreen */
        if (body.getPosition().y + sprite.getHeight() * sprite.getScaleY() / 2 < 0) free();
    }

    @Override
    public void die(float deltaTime) {
        PooledEffect p = screen.enemyDeathPool.obtain();
        p.setPosition(body.getPosition().x, body.getPosition().y);
        p.scaleEffect(scale * info.maxHp * PPM * 2);
        p.start();
        screen.effects.add(p);

        screen.setGameSpeed(5f);
        screen.addScore(info.maxHp);
        super.die(deltaTime);
        free();
    }

    /** Removes enemy from the active array of enemies and frees it from the pool. Sends body to the
     * array of dead bodies for processing. */
    public void free() {
        screen.enemies.removeValue(this, false);
        screen.enemyPool.free(this);
    }

    @Override
    public void reset() {
        light.remove(true);
        light = null;
        color = null;
        body = null;
        sprite = null;
        info.hp = info.maxHp;
    }
}
