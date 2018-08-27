package io.github.lionisaqt.actors;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import io.github.lionisaqt.screens.InGame;
import io.github.lionisaqt.utils.EntityInfo;

abstract class SpaceEntity {
    private World world;

    InGame screen;
    Body body;
    Sprite sprite;
    float scale, impact;
    short hp, maxHp, dmg, speed;
    boolean friendly, isPlayer;
    EntityInfo info;

    SpaceEntity(InGame screen) {
        this.screen = screen;
        world = screen.getWorld();
        info = new EntityInfo();
    }

    void makeBody(float x, float y, String s) {
        BodyDef bDef = new BodyDef();
        bDef.position.set(x, y);
        bDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bDef);

        FixtureDef fDef = new FixtureDef();
        switch (s) {
            case "circle":
                CircleShape shape = new CircleShape();
                shape.setRadius(sprite.getWidth() * sprite.getScaleX() * 2);
                fDef.shape = shape;
                fDef.isSensor = true;
                body.createFixture(fDef);
                shape.dispose();
                break;
            case "square":
            default:
                PolygonShape square = new PolygonShape();
                square.setAsBox(sprite.getWidth() * scale / 2, sprite.getHeight() * scale / 2);
                fDef.shape = square;
                body.createFixture(fDef);
                square.dispose();
                break;
        }
    }

    public void update(float deltaTime) { updateValues(); }

    public void draw(SpriteBatch batch) { sprite.draw(batch); }

    void setAll() {
        info.hp = hp;
        info.dmg = dmg;
        info.speed = speed;
        info.impact = impact;
        info.friendly = friendly;
        info.isPlayer = isPlayer;
    }

    private void updateValues() {
        hp = info.hp;
        dmg = info.dmg;
        speed = info.speed;
        impact = info.impact;
        friendly = info.friendly;
    }

    public void dispose() { sprite.getTexture().dispose(); }
}
