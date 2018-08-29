package io.github.lionisaqt.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;

import box2dLight.PointLight;
import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

import static io.github.lionisaqt.JuicyShmup.PPM;

/** The player Object.
* @author Ryan Shee */
public class Player extends SpaceEntity {
    /* The time since the last shot and how long the delay between shots are */
    private float shotTimer, fireDelay;

    /** Constructs a new player at the given coordinates.
     * @param game Reference to the game for assets
     * @param screen Reference for in-game stuff
     * @param x Initial x position
     * @param y Initial y position */
    public Player(JuicyShmup game, InGame screen, float x, float y) {
        super(screen);
        scale = 0.25f * PPM;
        fireDelay = 0.075f;
        info.maxHp = 500;
        info.hp = info.maxHp;
        info.dmg = 100;
        info.speed = 10;
        info.impact = 0f;
        info.friendly = true;

        if (sprite == null) {
            sprite = new Sprite(game.assets.manager.get(game.assets.ship));
            sprite.setScale(scale);
        }

        if (body == null) {
            makeBody(x, y, "square");
            body.setUserData(info);
            sprite.setPosition(body.getPosition().x, body.getPosition().y);
        }

        if (color == null)
            color = new Color(info.friendly ? 0 : 1, info.friendly ? 1 : 0, 0, 1);
        else
            color.set(info.friendly ? 0 : 1, info.friendly ? 1 : 0, 0, 1);

        if (light == null) {
            light = new PointLight(screen.rayHandler, 128, color, 150 * PPM, body.getPosition().x, body.getPosition().y);
            light.setStaticLight(false);
            light.setSoft(true);
            light.setPosition(body.getPosition().x, body.getPosition().y - 1);
        }
    }

    @Override
    public void update(float deltaTime) {
        if (info.hp <= 0) {
            die(deltaTime);
            return;
        }

        light.setPosition(body.getPosition().x, body.getPosition().y - 1.1f);

        handleInput(deltaTime);
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
                if (Gdx.input.isTouched()) { shoot(deltaTime); }
                break;
            case Desktop:
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                    xSpeed += -info.speed;
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                    xSpeed += info.speed;
                if (Gdx.input.isKeyPressed(Input.Keys.UP))
                    ySpeed += info.speed;
                if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
                    ySpeed += -info.speed;
                if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
                    shoot(deltaTime);
            default:
                break;
        }

        /* Caps speed */
        if (Math.abs(xSpeed) > info.speed) xSpeed = (xSpeed / Math.abs(xSpeed)) * info.speed;
        if (Math.abs(ySpeed) > info.speed) ySpeed = (ySpeed / Math.abs(ySpeed)) * info.speed;

        body.setLinearVelocity(xSpeed, ySpeed);
        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
        stayInBounds();
    }

    /** Fires a bullet, obtained from the pool. Adds trauma.
     * @param deltaTime Time since last frame was called */
    private void shoot(float deltaTime) {
        shotTimer -= deltaTime; // Run timer between shots
        if (shotTimer <= 0) {
            Bullet b = screen.bulletPool.obtain();                              // Obtain a bullet from pool, or creates one if a free bullet is unavailable
            b.init(body.getPosition().x, body.getPosition().y, true);   // Initializes bullet
            screen.bullets.add(b);                                              // Adds bullet to list of active bullets
            screen.tManager.addTrauma(b.info.impact);                           // Adds impact to trauma manager
            shotTimer += fireDelay;                                             // Add delay for next shot
        }
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
