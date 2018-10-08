package io.github.lionisaqt.actors;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import box2dLight.PointLight;
import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

import static io.github.lionisaqt.JuicyShmup.PPM;

/** Dogfighters stay on the field for longer to skirmish with the player.
 * @author Ryan Shee */
public class Dogfighter extends Enemy {
    private float
            shotTimer,
            fireDelay,
            reloadTimer;

    private final float reloadDuration;

    /* Muzzle flash lights */
    private PointLight muzzleFlash;

    /* Number of shots per burst */
    private final int burstNum;
    private int numShots;

    Dogfighter(JuicyShmup game, InGame screen, EnemyDirector director) {
        super(game, screen, director);
        scale = 0.25f * PPM;
        info.maxHp = 100;
        info.hp = info.maxHp;
        info.dmg = 10;
        info.impact = 0.3f;
        fireDelay = 0.2f;
        reloadDuration = 1f;
        reloadTimer = reloadDuration;
        burstNum = 10;
        numShots = burstNum;
    }

    @Override
    public void init() {
        super.init();

        sprite.setTexture(game.assets.manager.get(game.assets.dogfighter));
        initializeFlash();
    }

    @Override
    public void update(float deltaTime, int playerHp, Vector2 playerPos) {
        if (info.hp <= 0) {
            die();
            return;
        }

        if (playerHp > 0) {
	        if (body.getPosition().y > playerPos.y) {
		        body.setLinearVelocity(body.getLinearVelocity().x + (body.getPosition().x < playerPos.x ? 1 : -1) * (float)(-info.speed / 2 / Math.hypot(body.getPosition().x - playerPos.x, body.getPosition().y - playerPos.y)), info.speed);

		        // Stop tracking if within certain x range, used so it doesn't shake from decimal inequality
		        if (Math.abs(body.getPosition().x - playerPos.x) < 0.25f)
			        body.setLinearVelocity(0, body.getLinearVelocity().y);

		        // Begin engaging at certain range
		        if (Math.hypot(body.getPosition().x - playerPos.x, body.getPosition().y - playerPos.y) < 20) {
			        body.setLinearVelocity(body.getLinearVelocity().x, 0);

			        // Keep distance from player
			        if (Math.hypot(body.getPosition().x - playerPos.x, body.getPosition().y - playerPos.y) < 15)
				        body.setLinearVelocity(body.getLinearVelocity().x, - info.speed / 1.25f);

			        shoot(deltaTime);
		        }
	        } else body.setLinearVelocity(0, info.speed);
        } else body.setLinearVelocity(0, info.speed);

        muzzleFlash.setPosition(body.getPosition().x, body.getPosition().y - 1.25f);
        update(deltaTime);
    }

    @Override
    public void draw(SpriteBatch batch) { super.draw(batch); }

    /** Fires a bullet, obtained from the pool. Adds trauma.
     * @param deltaTime time since last frame was called */
    private void shoot(float deltaTime) {
        if (numShots > 0) {
            shotTimer -= deltaTime / screen.timeMultiplier;
            if (shotTimer <= 0) {
                numShots--;
                muzzleFlash.setActive(true);

                ParticleEffectPool.PooledEffect p = screen.eManager.enemyShotPool.obtain();
                p.setPosition(body.getPosition().x, body.getPosition().y - 1);
                p.scaleEffect(scale, -scale);
                p.start();
                screen.eManager.effects.add(p);

                Bullet b = screen.bulletPool.obtain();                              // Obtain a bullet from pool, or creates one if a free bullet is unavailable
                b.init(body.getPosition().x, body.getPosition().y, false);  // Initializes bullet
                screen.bullets.add(b);                                              // Adds bullet to list of active bullets
                shotTimer += fireDelay;                                             // Add delay for next shot
            } else {
                muzzleFlash.setActive(false);
            }
        } else {
            muzzleFlash.setActive(false);
            reloadTimer -= deltaTime / screen.timeMultiplier;
            if (reloadTimer <= 0) {
                numShots = burstNum;
                reloadTimer = reloadDuration;
            }
        }
    }

    /** Initializes muzzle flash animations and lights. */
    private void initializeFlash() {
        muzzleFlash = new PointLight(screen.eManager.rayHandler, 128, color, 100 * PPM, body.getPosition().x, body.getPosition().y);
        muzzleFlash.setStaticLight(false);
        muzzleFlash.setSoft(true);
        muzzleFlash.setActive(false);
    }

    @Override
    public void free() {
        director.enemies.removeValue(this, false);
        director.dogfighterPool.free(this);
        director.currDF--;
    }

    @Override
    public void reset() {
        super.reset();
        muzzleFlash.remove(true);
        muzzleFlash = null;
    }
}
