package io.github.lionisaqt.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.MassData;

import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

import static io.github.lionisaqt.JuicyShmup.PPM;

public class Player extends SpaceEntity {
    private float shotTimer, fireDelay;

    public Player(JuicyShmup game, InGame screen, float x, float y) {
        super(screen);
        scale = 0.25f * PPM;
        fireDelay = 0.1f;
        maxHp = 500;
        hp = maxHp;
        dmg = 100;
        speed = 10;
        impact = 0f;
        friendly = true;
        setAll();

        if (sprite == null) sprite = new Sprite(game.assets.manager.get(game.assets.ship));
        sprite.setScale(scale);

        makeBody(x, y, "square");
        body.setUserData(info);
        sprite.setPosition(body.getPosition().x, body.getPosition().y);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
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
                xSpeed = Gdx.input.getAccelerometerX() * -speed;
                ySpeed = Gdx.input.getAccelerometerY() * speed;

                break;
            case Desktop:
                if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
                    xSpeed += -speed;
                if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                    xSpeed += speed;
                if (Gdx.input.isKeyPressed(Input.Keys.UP))
                    ySpeed += speed;
                if (Gdx.input.isKeyPressed(Input.Keys.DOWN))
                    ySpeed += -speed;
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
            Bullet b = screen.bulletPool.obtain();     // Obtain a bullet from pool, or creates one if a free bullet is unavailable
            b.init(body.getPosition().x, body.getPosition().y, true);  // Initializes bullet
            screen.bullets.add(b);                     // Adds bullet to list of active bullets
            screen.tManager.addTrauma(b.getImpact());  // Adds impact to trauma manager
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
