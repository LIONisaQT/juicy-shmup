package io.github.lionisaqt.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.Random;

import static io.github.lionisaqt.JuicyShmup.PPM;

/** Manages camera shake in a semi-realistic manner.
 * @author Ryan Shee */
public class TraumaManager {
    /* Need a camera to shake */
    private final OrthographicCamera camera;

    /* Translational and angular offsets */
    private float trauma, maxAngle, angle, maxOffset, lastTrauma;

    /* The amount the camera will move by */
    private Vector2 offset;

    /** Constructs trauma manager.
     * @param camera The camera that will be affected */
    public TraumaManager(OrthographicCamera camera) {
        this.camera = camera;
        trauma = 0;
        maxAngle = 0f * PPM; // Swap to 32f for rotational shake
        angle = 0;
        maxOffset = 10f * PPM;
        lastTrauma = 0;
        offset = new Vector2();
    }

    /** Called every frame.
     * @param deltaTime Time since last frame was called */
    public final void manageShake(float deltaTime) {
        if (trauma > 0) {
            /* Calculates rotational shake */
            angle = maxAngle * shakeAmount() * (new Random().nextFloat() * 2 - 1);
            camera.rotate((-(float)Math.atan2(camera.up.x, camera.up.y) * MathUtils.radiansToDegrees) + angle);

            /* Calculates translational shake */
            offset.set(
                    maxOffset * shakeAmount() * (new Random().nextFloat() * 2 - 1),
                    maxOffset * shakeAmount() * (new Random().nextFloat() * 2 - 1)
            );
            camera.position.set(
                    camera.viewportWidth / 2 + offset.x,
                    camera.viewportHeight / 2 + offset.y,
                    0
            );

            decreaseTrauma(deltaTime);
        } else {
            /* Ensure camera gets put back into correct place */
            trauma = 0;
            camera.up.set(0, 1, 0);
            camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        }
    }

    /** Adds trauma to the shake. Capped.
     * @param t Trauma to be added */
    public final void addTrauma(float t) {
        if (trauma <= 1) {
            trauma += t;
            lastTrauma = trauma;
        }
    }

    /** Sets trauma level to exact value.
     * @param t Trauma level */
    public final void setTrauma(float t) { trauma = t; }

    /** Linearly decreases trauma over time.
     * @param deltaTime Time since last frame was called */
    public void decreaseTrauma(float deltaTime) { setTrauma(trauma -= deltaTime * 2); }

    public final float getMaxAngle() { return maxAngle; }
    public final float getMaxOffset() { return maxOffset; }

    public final float getTrauma() { return trauma; }
    public final float getLastTrauma() { return lastTrauma; }

    /** Scaling function for trauma.
     * @return Scaled float for camera shake amount. */
    public final float shakeAmount() { return trauma * trauma; }
}
