package io.github.lionisaqt.actors;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

import java.util.Random;

import box2dLight.PointLight;
import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

import static io.github.lionisaqt.JuicyShmup.PPM;

/** Poolable generic enemy class. Snakes and kamikazes into player.
 * @author Ryan Shee */
public class Enemy extends SpaceEntity implements Poolable {
    /* Reference to director for the pool and enemy array */
    EnemyDirector director;

    /* Enemy-specific death sound */
    private Sound enemyDeathSound;

    /** Constructs a new enemy.
     * @param game Reference to the game for assets
     * @param screen Reference for in-game stuff
     * @param director The enemy's manager */
    Enemy(JuicyShmup game, InGame screen, EnemyDirector director) {
        super(game, screen);
        this.director = director;
        scale = 0.2f * PPM;
        info.maxHp = 50;
        info.hp = info.maxHp;
        info.dmg = 100;
        info.speed = -8;
        info.impact = 0.25f;
        info.friendly = false;
        info.isPlayer = false;
        enemyDeathSound = game.assets.manager.get(game.assets.kill1);
    }

    /** Initializes important values if they're null, called after getting an enemy from the pool. */
    public void init() {
        if (sprite == null) {
            sprite = new Sprite(game.assets.manager.get(game.assets.kamikaze));
            sprite.flip(false, true);
            sprite.setScale(scale);
        }

        if (body == null) {
            makeBody((new Random().nextFloat() * JuicyShmup.GAME_WIDTH * PPM - sprite.getWidth() * sprite.getScaleX() * 2) + sprite.getWidth() * sprite.getScaleX() * 2, JuicyShmup.GAME_HEIGHT * PPM + (new Random().nextFloat() * 10 * sprite.getHeight() * sprite.getScaleY() / 2), "square");
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
            light = new PointLight(screen.eManager.rayHandler, 128, color, 100 * PPM, body.getPosition().x, body.getPosition().y);
            light.setStaticLight(false);
            light.setSoft(true);
            light.setPosition(body.getPosition().x, body.getPosition().y + 1.1f);
        }

        deathSound = new Random().nextBoolean() ? game.assets.manager.get(game.assets.death1) : game.assets.manager.get(game.assets.death2);
    }

    @Override
    public void update(float deltaTime) {
        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
        light.setPosition(body.getPosition().x, body.getPosition().y + 1);

        /* Engine particle effects! */
        PooledEffect p = screen.eManager.enemyEnginePool.obtain();
        p.setPosition(body.getPosition().x, body.getPosition().y + 0.75f);
        p.scaleEffect(scale);
        p.start();
        screen.eManager.effects.add(p);

        /* Checks to make sure body speed is constant */
        if (Math.abs(body.getLinearVelocity().x) >= Math.abs(info.speed)) body.setLinearVelocity(body.getLinearVelocity().x > 0 ? -info.speed : info.speed, body.getLinearVelocity().y);

        /* Kills enemy if offscreen */
        if (body.getPosition().y + sprite.getHeight() * sprite.getScaleY() / 2 < 0) free();
    }

    /** Enemy-specific update method. Default enemy tries to snake and kamikaze into player.
     * @param deltaTime Time since last frame was called
     * @param playerHp Player's current hp
     * @param  playerPos Position to target */
    public void update(float deltaTime, int playerHp, Vector2 playerPos) {
        if (info.hp <= 0) {
            die();
            return;
        }

        if (playerHp > 0)
	        body.setLinearVelocity(body.getLinearVelocity().x + (body.getPosition().x < playerPos.x ? 1 : -1) * (float)(-info.speed / 4 / Math.hypot(body.getPosition().x - playerPos.x, body.getPosition().y - playerPos.y)), info.speed);
        else
        	body.setLinearVelocity(0, info.speed);

        update(deltaTime);
    }

    @Override
    public void die() {
    	deathSound.play();
    	enemyDeathSound.play();

        PooledEffect p = screen.eManager.enemyDeathPool.obtain();
        p.setPosition(body.getPosition().x, body.getPosition().y);
        p.scaleEffect(scale * 5);
        p.start();
        screen.eManager.effects.add(p);

        screen.setGameSpeed(2f);
        screen.addScore(info.maxHp);
        super.die();
        free();
    }

    /** Removes enemy from the active array of enemies and frees it from the pool. Sends body to the
     * array of dead bodies for processing. */
    public void free() {
        director.enemies.removeValue(this, false);
        director.enemyPool.free(this);
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
