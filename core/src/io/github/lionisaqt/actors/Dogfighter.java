package io.github.lionisaqt.actors;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

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
            stateTimer;

    /* The muzzle flash animation */
    private Animation<TextureRegion> shoot;

    /* Left and right muzzle flashes */
    private Sprite flashLeft, flashRight;

    /* Controls when the muzzle flash plays */
    private boolean isShooting;

    /* Muzzle flash lights */
    private PointLight muzzleLightLeft, muzzleLightRight;

    Dogfighter(JuicyShmup game, InGame screen, EnemyDirector director) {
        super(game, screen, director);
        info.maxHp = 100;
        info.hp = info.maxHp;
        info.dmg = 10;
        fireDelay = 0.5f;
    }

    @Override
    public void init() {
        super.init();
        sprite.setTexture(game.assets.manager.get(game.assets.ship));
        sprite.flip(false, true);
        initializeFlash();
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        if (alive) {
            muzzleLightLeft.setPosition(body.getPosition().x - 1.25f, body.getPosition().y - 0.75f);
            muzzleLightRight.setPosition(body.getPosition().x + 1.25f, body.getPosition().y - 0.75f);

            flashLeft.setPosition(body.getPosition().x - 2, body.getPosition().y - 1f);
            flashRight.setPosition(body.getPosition().x, body.getPosition().y - 1f);
        }
    }


    @Override
    public void update(float deltaTime, Vector2 playerPos) {
        if (info.hp <= 0) {
            die();
            return;
        }

        shoot(deltaTime);
        if (isShooting) {
            Random rng = new Random();
            muzzleLightLeft.setActive(rng.nextBoolean());
            muzzleLightRight.setActive(rng.nextBoolean());
            flashRight.flip(true, false);
        }

        update(deltaTime);
    }

    @Override
    public void draw(SpriteBatch batch) {
        super.draw(batch);

        if (isShooting) {
            flashLeft.draw(batch);
            flashRight.draw(batch);
        }
    }

    /** Fires a bullet, obtained from the pool. Adds trauma.
     * @param deltaTime time since last frame was called */
    private void shoot(float deltaTime) {
        isShooting = true;
        shotTimer -= deltaTime / screen.timeMultiplier;

        /* Play animation based on time */
        flashLeft.setRegion(shoot.getKeyFrame(stateTimer));
        flashRight.setRegion(shoot.getKeyFrame(stateTimer));

        if (shotTimer <= 0) {
            ParticleEffectPool.PooledEffect p = screen.eManager.shotPool.obtain();
            p.setPosition(body.getPosition().x, body.getPosition().y - 1);
            p.scaleEffect(scale);
            p.start();
            screen.eManager.effects.add(p);

            Bullet b = screen.bulletPool.obtain();                              // Obtain a bullet from pool, or creates one if a free bullet is unavailable
            b.init(body.getPosition().x, body.getPosition().y, false);  // Initializes bullet
            screen.bullets.add(b);                                              // Adds bullet to list of active bullets
            shotTimer += fireDelay;                                             // Add delay for next shot
        }

        stateTimer += deltaTime / 10;                                            // Adds time to animation timer
    }

    /** Initializes muzzle flash animations and lights. */
    private void initializeFlash() {
        TextureRegion flashAnim = new TextureRegion(game.assets.manager.get(game.assets.flash), 0, 0, 32, 32);
        Array<TextureRegion> frames = new Array<>();
        for (int i = 0; i < 6; i++)
            frames.add(new TextureRegion(game.assets.manager.get(game.assets.flash), i * 32, 0, 32, 32));
        shoot = new Animation<>(0.01f, frames, Animation.PlayMode.LOOP);
        frames.clear();
        stateTimer = 0;

        flashLeft = new Sprite();
        flashLeft.setBounds(0, 0, 32 * PPM, 32 * PPM);
        flashLeft.setRegion(flashAnim);
        flashLeft.setScale(2, 1);

        flashRight = new Sprite();
        flashRight.setBounds(0, 0, 32 * PPM, 32 * PPM);
        flashRight.setRegion(flashAnim);
        flashRight.setScale(flashLeft.getScaleX(), flashLeft.getScaleY());

        muzzleLightLeft = new PointLight(screen.eManager.rayHandler, 128, color, 100 * PPM, body.getPosition().x, body.getPosition().y);
        muzzleLightLeft.setStaticLight(false);
        muzzleLightLeft.setSoft(true);

        muzzleLightRight = new PointLight(screen.eManager.rayHandler, 128, color, 100 * PPM, body.getPosition().x, body.getPosition().y);
        muzzleLightRight.setStaticLight(false);
        muzzleLightRight.setSoft(true);
    }
}
