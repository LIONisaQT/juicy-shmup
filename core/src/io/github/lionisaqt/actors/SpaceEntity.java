package io.github.lionisaqt.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;

import box2dLight.PointLight;
import io.github.lionisaqt.screens.InGame;
import io.github.lionisaqt.utils.EntityInfo;

/**
 * Abstract class for generic space objects.
 * @author Ryan Shee */
abstract class SpaceEntity extends Sprite {
    World world;

    /* Reference for in-game stuff */
    InGame screen;

    /* Physics body */
    Body body;

    /* Rendered image */
    Sprite sprite;

    /* Used to convert sprite pixels to box2d meters */
    float scale;

    /* The object with the entity's information to be passed into the body's user data */
    EntityInfo info;

    /* Light emitted by this entity */
    PointLight light;

    /* Light color to help tell friendly from non-friendly */
    Color color;

    /** Constructs a space entity.
     * @param screen Reference for in-game stuff */
    SpaceEntity(InGame screen) {
        this.screen = screen;
        world = screen.world;
        info = new EntityInfo();
    }

    /**
     * Makes the entire body of the entity, along with its fixtures.
     * @param x The initial x position of the body
     * @param y The initial y position of the body
     * @param s The shape of the body. So far it's just "circle" or "square". */
    void makeBody(float x, float y, String s) {
        BodyDef bDef = new BodyDef();
        bDef.position.set(x, y);
        bDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bDef);

        FixtureDef fDef = new FixtureDef();
        Shape shape;

        switch (s) {
            case "circle":
                shape = new CircleShape();
                shape.setRadius(sprite.getWidth() * sprite.getScaleX() * 2);
                fDef.isSensor = true;
                break;
            case "square":
            default:
                shape = new PolygonShape();
                ((PolygonShape)shape).setAsBox(sprite.getWidth() * scale / 2, sprite.getHeight() * scale / 2);
                break;
        }

        fDef.shape = shape;
        body.createFixture(fDef);
        shape.dispose();
    }

    /**
     * Called every frame. Handles any logic with the entity.
     * @param deltaTime Time since last frame was called */
    public abstract void update(float deltaTime);

    /** Some things may do things other things when they die.
     * @param deltaTime Time since last frame was called */
    public void die(float deltaTime) {
        screen.world.destroyBody(body);
        screen.tManager.addTrauma(info.impact);
    }

    /** Draws the entity.
     * @param batch The SpriteBatch for batch drawing. */
    public void draw(SpriteBatch batch) { sprite.draw(batch); }
}
