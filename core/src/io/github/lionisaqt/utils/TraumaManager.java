package io.github.lionisaqt.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

import io.github.lionisaqt.actors.Player;

import static io.github.lionisaqt.JuicyShmup.PPM;

public class TraumaManager {
    private final Player player;
    private final OrthographicCamera camera;
    private float trauma, maxAngle, angle, maxOffset, lastTrauma;
    private Vector2 offset;

    public TraumaManager(OrthographicCamera camera, Player player) {
        this.player = player;
        this.camera = camera;
        trauma = 0;
        maxAngle = 32f * PPM;
        angle = 0;
        maxOffset = 0f * PPM;
        lastTrauma = 0;
        offset = new Vector2();
    }

    public final void manageShake(float deltaTime) {
        if (trauma > 0) {
            angle = maxAngle * shakeAmount() * (new Random().nextFloat()*2-1);
            camera.rotate((-(float)Math.atan2(camera.up.x, camera.up.y) * MathUtils.radiansToDegrees) + angle);

            offset.set(
                    maxOffset * shakeAmount() * (new Random().nextFloat()*2-1),
                    maxOffset * shakeAmount() * (new Random().nextFloat()*2-1)
            );
            camera.position.set(
                    player.body.getPosition().x + offset.x,
                    player.body.getPosition().y + offset.y,
                    0
            );
            decreaseTrauma(deltaTime);
        } else {
            trauma = 0;
            camera.up.set(0, 1, 0);
            camera.position.set(player.body.getPosition().x, player.body.getPosition().y, 0);
        }
    }

    public final void addTrauma(float t) {
        if (trauma <= 1) {
            trauma += t;
            lastTrauma = trauma;
        }
    }

    public final void setTrauma(float t) { trauma = t; }

    public void decreaseTrauma(float deltaTime) { setTrauma(trauma -= deltaTime); }

    public final float getMaxAngle() { return maxAngle; }
    public final float getMaxOffset() { return maxOffset; }

    public final float getTrauma() { return trauma; }

    public final float getLastTrauma() { return lastTrauma; }

    public final float shakeAmount() { return getTrauma() * getTrauma(); }
}