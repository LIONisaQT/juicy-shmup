package io.github.lionisaqt.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

import box2dLight.PointLight;
import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

import static io.github.lionisaqt.JuicyShmup.PPM;

/** The player Object.
* @author Ryan Shee */
public class Player extends SpaceEntity {
    private float
            shotTimer,  // Time since last shot
            fireDelay,  // How long the delay between shots are
            stateTimer; // Time between animation frames

    /* The muzzle flash animation */
    private Animation<TextureRegion> shoot;

    /* Left and right muzzle flashes */
    private Sprite flashLeft, flashRight;

    /* Controls when the muzzle flash plays */
    private boolean isShooting;

    /* Muzzle flash lights */
    private PointLight muzzleLightLeft, muzzleLightRight;

    /* Ensures death effect only plays once */
    private boolean deathEffectPlay = true;

    /** Constructs a new player at the given coordinates.
     * @param game Reference to the game for assets
     * @param screen Reference for in-game stuff
     * @param x Initial x position
     * @param y Initial y position */
    public Player(JuicyShmup game, InGame screen, float x, float y) {
        super(game, screen);
        scale = 0.25f * PPM;
        fireDelay = 0.1f;
        info.maxHp = 1000;
        info.hp = info.maxHp;
        info.dmg = 100;
        info.speed = 15;
        info.impact = 1f;
        info.friendly = true;
        info.isPlayer = true;

        sprite = new Sprite(game.assets.manager.get(game.assets.ship));
        sprite.setScale(scale);

        makeBody(x, y, "square");
        body.setUserData(info);
        sprite.setPosition(body.getPosition().x, body.getPosition().y);

        color = new Color(info.friendly ? 0 : 1, info.friendly ? 1 : 0, 0, 1);

        /* Engine light */
        light = new PointLight(screen.eManager.rayHandler, 128, color, 150 * PPM, body.getPosition().x, body.getPosition().y);
        light.setStaticLight(false);
        light.setSoft(true);
        light.setPosition(body.getPosition().x, body.getPosition().y - 1);

        initializeFlash();
    }

    @Override
    public void update(float deltaTime) {
        if (info.hp <= 0) {
            playerDie();
            return;
        }

        handleInput(deltaTime);

        /* Turn off muzzle flashes */
        if (muzzleLightLeft.isActive()) muzzleLightLeft.setActive(false);
        if (muzzleLightRight.isActive()) muzzleLightRight.setActive(false);

        /* Except when shooting */
        if (isShooting) {
            Random rng = new Random();
            muzzleLightLeft.setActive(rng.nextBoolean());
            muzzleLightRight.setActive(rng.nextBoolean());
            flashRight.flip(true, false);
        }

        /* Engine particle effects! */
        PooledEffect p = screen.eManager.enginePool.obtain();
        p.setPosition(body.getPosition().x, body.getPosition().y - 1);
        p.scaleEffect(scale);
        p.start();
        screen.eManager.effects.add(p);
    }

    /** Handles input and sets velocity accordingly, and makes sprite follow body.
     * @param deltaTime Time since the last frame was called */
    private void handleInput(float deltaTime) {
        float xSpeed = 0;
        float ySpeed = 0;

        switch (Gdx.app.getType()) {
            case Android:
            case iOS:
                xSpeed = Gdx.input.getAccelerometerX() * -info.speed;
                ySpeed = Gdx.input.getAccelerometerY() * -info.speed;
                if (Gdx.input.isTouched()) shoot(deltaTime);
                else isShooting = false;
                break;
            case Desktop:
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) xSpeed += -info.speed;
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) xSpeed += info.speed;
                if (Gdx.input.isKeyPressed(Input.Keys.UP)) ySpeed += info.speed;
                if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) ySpeed += -info.speed;
                if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) shoot(deltaTime);
                if (Gdx.input.isKeyJustPressed(Input.Keys.K)) info.hp = 0;
                else isShooting = false;
            default:
                break;
        }

        /* Caps speed */
        if (Math.abs(xSpeed) > info.speed) xSpeed = (xSpeed / Math.abs(xSpeed)) * info.speed;
        if (Math.abs(ySpeed) > info.speed) ySpeed = (ySpeed / Math.abs(ySpeed)) * info.speed;

        body.setLinearVelocity(xSpeed, ySpeed);

        light.setPosition(body.getPosition().x, body.getPosition().y - 1.1f);
        muzzleLightLeft.setPosition(body.getPosition().x - 1.25f, body.getPosition().y + 1);
        muzzleLightRight.setPosition(body.getPosition().x + 1.25f, body.getPosition().y + 1);

        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
        flashLeft.setPosition(body.getPosition().x - 2, body.getPosition().y + 0.25f);
        flashRight.setPosition(body.getPosition().x, body.getPosition().y + 0.25f);

        stayInBounds();
    }

    /** Fires a bullet, obtained from the pool. Adds trauma.
     * @param deltaTime time since last frame was called */
    private void shoot(float deltaTime) {
        isShooting = true;
        shotTimer -= deltaTime / screen.timeMultiplier; // Run timer between shots

        /* Play animation based on time */
        flashLeft.setRegion(shoot.getKeyFrame(stateTimer));
        flashRight.setRegion(shoot.getKeyFrame(stateTimer));

        if (shotTimer <= 0) {
            PooledEffect p = screen.eManager.shotPool.obtain();
            p.setPosition(body.getPosition().x, body.getPosition().y + 1);
            p.scaleEffect(scale);
            p.start();
            screen.eManager.effects.add(p);

            Bullet b = screen.bulletPool.obtain();                              // Obtain a bullet from pool, or creates one if a free bullet is unavailable
            b.init(body.getPosition().x, body.getPosition().y, true);   // Initializes bullet
            screen.bullets.add(b);                                              // Adds bullet to list of active bullets
            screen.tManager.addTrauma(b.info.impact);                           // Adds impact to trauma manager
            shotTimer += fireDelay;                                             // Add delay for next shot
            stateTimer += deltaTime;                                            // Adds time to animation timer
        }
    }

    /** Player-specific death effects. */
    private void playerDie() {
    	if (deathEffectPlay) {
    		deathEffectPlay = false;
		    body.setActive(false);

		    sprite.setAlpha(0);
		    flashLeft.setAlpha(0);
		    flashRight.setAlpha(0);

		    muzzleLightLeft.setActive(false);
		    muzzleLightRight.setActive(false);
		    light.setActive(false);

		    screen.tManager.addTrauma(info.impact);

		    // Explosion light effect
		    PointLight p = screen.eManager.lightPool.obtain();
		    p.setColor(color);
		    p.setDistance(2500 * info.impact * PPM);
		    p.setPosition(body.getPosition());
		    screen.eManager.lightEffects.add(p);
		    game.currentSong.setVolume(0.25f);

		    for (Enemy e : screen.director.enemies) e.info.hp = 0;
	    }

    	screen.timeMultiplier = 5f;
    }

    public void draw(SpriteBatch batch) {
        super.draw(batch);
        if (isShooting) {
            flashLeft.draw(batch);
            flashRight.draw(batch);
        }
    }

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

    /** Helper method that ensures player body stays within screen. */
    private void stayInBounds() {
        // Stay within X bounds
        if (body.getPosition().x + sprite.getWidth() * sprite.getScaleX() / 2 > JuicyShmup.GAME_WIDTH * PPM)
            body.setTransform(JuicyShmup.GAME_WIDTH * PPM - sprite.getWidth() * sprite.getScaleX() / 2, body.getPosition().y, body.getAngle());
        else if (body.getPosition().x - sprite.getWidth() * sprite.getScaleX() / 2 < 0)
            body.setTransform(sprite.getWidth() * sprite.getScaleX() / 2, body.getPosition().y, body.getAngle());

        // Stay within Y bounds
        if (body.getPosition().y + sprite.getHeight() * sprite.getScaleY() / 2 > JuicyShmup.GAME_HEIGHT * PPM)
            body.setTransform(body.getPosition().x, JuicyShmup.GAME_HEIGHT * PPM - sprite.getHeight() * sprite.getScaleY() / 2, body.getAngle());
        else if (body.getPosition().y - sprite.getHeight() * sprite.getScaleY() / 2 < 0)
            body.setTransform(body.getPosition().x, sprite.getHeight() * sprite.getScaleY() / 2, body.getAngle());
    }
}
