package io.github.lionisaqt.actors;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool.Poolable;

import box2dLight.PointLight;
import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

import static io.github.lionisaqt.JuicyShmup.PPM;

public class Bullet extends SpaceEntity implements Poolable {
    private JuicyShmup game;
    private PointLight bLight;
    private Color bColor;

    public Bullet(JuicyShmup game, InGame screen) {
        super(screen);
        this.game = game;
        maxHp = 1;
        hp = maxHp;
        dmg = 10;
        speed = 25;
        impact = 0.1f;
        isPlayer = false;
    }

    public void init(float x, float y, boolean friendly) {
        if (sprite == null) {
            sprite = new Sprite(game.assets.manager.get(game.assets.bullet));
            sprite.setScale(0.1f * PPM);
        }

        if (body == null) makeBody(x, y, "circle");
        body.setBullet(true);
        this.friendly = friendly;
        setAll();
        body.setUserData(info);
        body.setLinearVelocity(0, friendly ? speed : -speed);

        if (bColor == null)
            bColor = new Color(friendly ? 0 : 1, friendly ? 1 : 0, 0, 1);
        else
            bColor.set(friendly ? 0 : 1, friendly ? 1 : 0, 0, 1);

        if (bLight == null) bLight = new PointLight(screen.rayHandler, 128, bColor, 50 * PPM, body.getPosition().x, body.getPosition().y);
        bLight.setStaticLight(false);
        bLight.setSoft(true);
        bLight.attachToBody(body);

        // TODO: Play sound here
    }

    public void update(float deltaTime) {
        super.update(deltaTime);
        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);

        if (hp <= 0 || body.getPosition().y + sprite.getHeight() * sprite.getScaleY() / 2 > JuicyShmup.GAME_HEIGHT  * PPM) {
            bLight.setActive(false);
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
        for (Fixture f : body.getFixtureList())
            body.destroyFixture(f);
        body = null;
        sprite = null;
        bColor = null;
        bLight = null;
        hp = maxHp;
        setAll();
    }

    public void dispose() {
        super.dispose();
        bLight.dispose();
    }
}
