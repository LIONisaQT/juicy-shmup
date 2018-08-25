package io.github.lionisaqt.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

import box2dLight.PointLight;
import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

import static io.github.lionisaqt.JuicyShmup.PPM;

public class Bullet extends SpaceEntity implements Poolable {
    private PointLight bLight;
    private Color bColor;
    private float speed, impact;

    public Bullet(JuicyShmup game, InGame screen) {
        super(screen);
        if (sprite == null) sprite = new Sprite(game.assets.manager.get(game.assets.bullet));
        sprite.setScale(0.5f * PPM);
        bColor = new Color(1, 1, 1, 1);
        bLight = new PointLight(screen.rayHandler, 128, bColor, 50 * PPM, 0, 0);
        bLight.setStaticLight(false);
        bLight.setSoft(true);
    }

    // CR799889038

    public void init(Vector2 v, boolean friendly) {
        speed = 15;
        impact = 1f;

        bColor = friendly ? bColor.set(0, 1, 0, 1) : bColor.set(1, 0, 0, 1);

        // TODO: Play sound here

        makeBody(v.x, v.y, "circle");
        body.setBullet(true);
        body.setUserData(friendly ? "friendly" : "enemy");
        body.setLinearVelocity(0, friendly ? speed : -speed);

        bLight.setPosition(body.getPosition());
        bLight.setColor(bColor);
    }

    public void update(float deltaTime) {
        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);
        bLight.setPosition(body.getPosition());

        if (body.getPosition().y + sprite.getHeight() * sprite.getScaleY() / 2 > JuicyShmup.GAME_HEIGHT  * PPM) {
            bLight.setColor(bColor.r, bColor.g, bColor.b, 0);
            free();
        }
    }

    public void draw(SpriteBatch batch) { sprite.draw(batch); }

    public float getImpact() { return impact; }

    public void free() {
        screen.bullets.removeValue(this, false);
        screen.bulletPool.free(this);
    }

    @Override
    public void reset() {
        impact = 0;
        speed = 0;
    }
}
