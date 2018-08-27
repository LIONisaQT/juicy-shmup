package io.github.lionisaqt.actors;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Pool.Poolable;

import java.util.Random;

import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

import static io.github.lionisaqt.JuicyShmup.PPM;

public class Enemy extends SpaceEntity implements Poolable {
    private JuicyShmup game;

    public Enemy(JuicyShmup game, InGame screen) {
        super(screen);
        this.game = game;
        scale = 0.25f * PPM;
        maxHp = 100;
        hp = maxHp;
        dmg = 25;
        speed = -2;
        impact = 1f;
        friendly = false;
        isPlayer = false;
        setAll();
    }

    public void init() {
        if (sprite == null) {
            sprite = new Sprite(game.assets.manager.get(game.assets.img));
            sprite.setScale(scale);
        }

        if (body == null) makeBody(new Random().nextFloat() * JuicyShmup.GAME_WIDTH * PPM - sprite.getWidth() * sprite.getScaleX() / 2, JuicyShmup.GAME_HEIGHT * PPM - sprite.getHeight() * sprite.getScaleY() / 2, "square");
        setAll();
        body.setUserData(info);
        body.setLinearVelocity(0, speed);
        sprite.setPosition(body.getPosition().x, body.getPosition().y);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        sprite.setPosition(body.getPosition().x - sprite.getWidth() / 2, body.getPosition().y - sprite.getHeight() / 2);

        if (body.getLinearVelocity().y != speed) {
            body.setLinearVelocity(body.getLinearVelocity().x, speed);
        }

        if (body.getPosition().y - sprite.getHeight() * sprite.getScaleY() / 2 < 0) {
            body.setTransform(new Random().nextFloat() * JuicyShmup.GAME_WIDTH * PPM - sprite.getWidth() * sprite.getScaleX() / 2, JuicyShmup.GAME_HEIGHT * PPM, body.getAngle());
        }

        if (hp <= 0) {
            screen.tManager.addTrauma(impact);
            info.hp = maxHp;
            PooledEffect p = screen.effectPool.obtain();
            p.setPosition(body.getPosition().x, body.getPosition().y);
            p.scaleEffect(scale * maxHp / 10);
            p.start();
            screen.effects.add(p);
            free();
        }
    }

    public void free() {
        screen.enemies.removeValue(this, false);
        screen.enemyPool.free(this);
    }

    @Override
    public void reset() {
        for (Fixture f : body.getFixtureList())
            body.destroyFixture(f);
        body = null;
        sprite = null;
        hp = maxHp;
        setAll();
    }
}
