package io.github.lionisaqt.actors;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool.Poolable;

import java.util.Random;

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
        info.dmg = 25;
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
    }

    @Override
    public void update(float deltaTime) {
        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);

        /* Checks to make sure body speed is constant */
        if (body.getLinearVelocity().y != info.speed) {
            body.setLinearVelocity(body.getLinearVelocity().x, info.speed);
        }

        /* Kills enemy if offscreen */
        if (body.getPosition().y - sprite.getHeight() * sprite.getScaleY() / 2 < 0) free();

        /* Enemy dies from causes. Rewards player with particle effects and camera shake. */
        if (info.hp <= 0) {
            screen.tManager.addTrauma(info.impact);
            PooledEffect p = screen.effectPool.obtain();
            p.setPosition(body.getPosition().x, body.getPosition().y);
            p.scaleEffect(scale * info.maxHp / 10);
            p.start();
            screen.effects.add(p);
            free();
        }
    }

    /** Removes enemy from the active array of enemies and frees it from the pool. */
    public void free() {
        screen.enemies.removeValue(this, false);
        screen.enemyPool.free(this);
    }

    @Override
    public void reset() {
        /* Destroys the body, otherwise physics would still be calculating for this object */
        for (Fixture f : body.getFixtureList()) body.destroyFixture(f);

        body = null;
        sprite = null;
        info.hp = info.maxHp;
    }
}
