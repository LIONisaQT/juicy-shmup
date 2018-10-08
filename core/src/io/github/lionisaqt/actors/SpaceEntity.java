package io.github.lionisaqt.actors;

import com.badlogic.gdx.audio.Sound;
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
import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;
import io.github.lionisaqt.utils.EntityInfo;

import static io.github.lionisaqt.JuicyShmup.PPM;

/**
 * Abstract class for generic space objects.
 * @author Ryan Shee */
abstract class SpaceEntity extends Sprite {
    /* Reference to the game for assets */
    JuicyShmup game;
    private World world;    // Need world to build body
    InGame screen;          // Reference for in-game stuff
    public Body body;       // Physics body
    Sprite sprite;          // Rendered image
    float scale;            // Used to convert pixels to box2d meters
    public EntityInfo info;        // Contains entity's information, passed into body's user data
    PointLight light;       // Light emitted by this entity
    Color color;            // Light color to help tell friend from foe
    Sound deathSound;       // Generic death sound

    /** Constructs a space entity.
     * @param screen Reference for in-game stuff */
    SpaceEntity(JuicyShmup game, InGame screen) {
        this.game = game;
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

        /* What category an object is */
        fDef.filter.categoryBits = info.friendly ? JuicyShmup.PLAYER_BIT : JuicyShmup.ENEMY_BIT | JuicyShmup.ITEM_BIT;

        /* What categories they collide with */
        fDef.filter.maskBits = info.friendly ? JuicyShmup.ENEMY_BIT | JuicyShmup.ITEM_BIT : JuicyShmup.PLAYER_BIT;

        /*
        * Special case for Dogfighters to prevent pile-up
        * TODO: Make dogfighters pile up side by side (use horizontal "spaces" in front of the player can they can fill, like Vermintide 2)
        * */
	    if (getClass().getSimpleName().equals("Dogfighter")) {
	    	fDef.filter.categoryBits = JuicyShmup.DF_BIT;
	    	fDef.filter.maskBits = JuicyShmup.DF_BIT;
	    }

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

    /** Some things may do things other things when they die. */
    public void die() {
        /* Explosion light effect */
        PointLight p = screen.eManager.lightPool.obtain();
        p.setColor(color);
        p.setDistance(1500 * info.impact * PPM);
        p.setPosition(body.getPosition());
        screen.eManager.lightEffects.add(p);

        body.setActive(false);
        if (!body.isActive()) screen.world.destroyBody(body);

        screen.tManager.addTrauma(info.impact);
    }

    /** Draws the entity.
     * @param batch The SpriteBatch for batch drawing. */
    public void draw(SpriteBatch batch) { sprite.draw(batch); }
}
