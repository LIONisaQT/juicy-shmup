package io.github.lionisaqt.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;

import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

import static io.github.lionisaqt.JuicyShmup.PPM;

public class Player extends SpaceEntity {
    private float shotTimer, fireDelay, turnSpeedKey, turnSpeedTouch;

    public Player(JuicyShmup game, InGame screen, float x, float y) {
        super(screen);
        turnSpeedKey = 250 * PPM;
        turnSpeedTouch = -250 * PPM;
        scale = 0.25f * PPM;
        fireDelay = 0.1f;
        if (sprite == null) sprite = new Sprite(game.assets.manager.get(game.assets.ship));
        sprite.setScale(scale);

        makeBody(x, y, "square");
        body.setUserData("player");
        sprite.setPosition(body.getPosition().x, body.getPosition().y);
    }

    @Override
    public void update(float deltaTime) {
        handleInput(deltaTime);
    }
    
    private void handleInput(float deltaTime) {
        float xSpeed = 0;
        float ySpeed = 0;

        switch (Gdx.app.getType()) {
            case Android:
            case iOS:
//                TODO: Figure out on-screen controller for more precise movement
//                if (Gdx.input.isTouched()) {
//                    if (Gdx.input.getX() < Gdx.graphics.getWidth() / 2)
//                        xSpeed = -turnSpeedKey;
//                    if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2)
//                        xSpeed = turnSpeedKey;
//                }

                if (Gdx.input.isTouched()) { shoot(deltaTime); }

                // Accelerometer movement
                xSpeed = Gdx.input.getAccelerometerX() * turnSpeedTouch;
                ySpeed = Gdx.input.getAccelerometerY() * turnSpeedTouch;

                break;
            case Desktop:
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                    xSpeed += -turnSpeedKey;
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                    xSpeed += turnSpeedKey;
                if (Gdx.input.isKeyPressed(Input.Keys.UP))
                    ySpeed += turnSpeedKey;
                if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
                    ySpeed += -turnSpeedKey;
                if (Gdx.input.isKeyPressed(Input.Keys.SPACE))
                    shoot(deltaTime);
            default:
                break;
        }

        body.setLinearVelocity(xSpeed, ySpeed);
        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
        stayInBounds();
    }

    private void shoot(float deltaTime) {
        shotTimer -= deltaTime; // Run timer between shots
        if (shotTimer <= 0) {
            Bullet bullet = screen.bulletPool.obtain();     // Obtain a bullet from pool, or creates one if a free bullet is unavailable
            bullet.init(body.getPosition(), true);  // Initializes bullet
            screen.bullets.add(bullet);                     // Adds bullet to list of active bullets
            screen.tManager.addTrauma(bullet.getImpact());  // Adds impact to trauma manager
            shotTimer += fireDelay;                         // Add delay for next shot
        }
    }

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
