package io.github.lionisaqt.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import io.github.lionisaqt.JuicyShmup;

import static io.github.lionisaqt.JuicyShmup.PPM;

public class EffectsManager {
    public Array<PooledEffect> effects;

    /* Particle pools  */
    public ParticleEffectPool
            effectPool,                     // Generic explosion
            enemyDeathPool,                 // Enemy death
            shotPool,                       // Player shooting
            enemyShotPool,                  // Enemy shooting
            enginePool,                     // Player engine
            enemyEnginePool,                // Enemy engine
            tracersPool,                    // Friendly bullet tracer
            enemyTracersPool;               // Enemy bullet tracer

    public RayHandler rayHandler;
    public Array<PointLight> lightEffects;  // Holds all light effects for dying entities
    public Array<PointLight> bgStars;       // Background stars
    private final short numStars = 35;      // Number of background stars
    private final short starSpeed = 9;      // Maximum speed of stars
    public Pool<PointLight> lightPool;      // Pointlight pool for entity deaths

    /** Helper function that loads all the particles and particle pools. */
    public void loadParticles() {
        effects = new Array<>();
        ParticleEffect explosion = new ParticleEffect();
        explosion.load(Gdx.files.internal("effects/explosion.p"), Gdx.files.internal("effects/"));
        effectPool = new ParticleEffectPool(explosion, 1, 100);

        /* Enemy death */
        ParticleEffect eDeath = new ParticleEffect();
        eDeath.load(Gdx.files.internal("effects/enemy_death.p"), Gdx.files.internal("effects/"));
        enemyDeathPool = new ParticleEffectPool(eDeath, 1, 100);

        /* Player shoot */
        ParticleEffect pShoot = new ParticleEffect();
        pShoot.load(Gdx.files.internal("effects/muzzle_flash.p"), Gdx.files.internal("effects/"));
        shotPool = new ParticleEffectPool(pShoot, 1, 100);

        /* Enemy shoot */
        ParticleEffect eShoot = new ParticleEffect();
        eShoot.load(Gdx.files.internal("effects/enemy_muzzle_flash.p"), Gdx.files.internal("effects/"));
        enemyShotPool = new ParticleEffectPool(eShoot, 1, 100);

        /* Player engine */
        ParticleEffect engine = new ParticleEffect();
        engine.load(Gdx.files.internal("effects/engine.p"), Gdx.files.internal("effects/"));
        enginePool = new ParticleEffectPool(engine, 1, 100);

        /* Enemy engine */
        ParticleEffect eEngine = new ParticleEffect();
        eEngine.load(Gdx.files.internal("effects/enemy_engine.p"), Gdx.files.internal("effects/"));
        enemyEnginePool = new ParticleEffectPool(eEngine, 1, 100);

        /* Bullet tracers */
        ParticleEffect tracer = new ParticleEffect();
        tracer.load(Gdx.files.internal("effects/tracer.p"), Gdx.files.internal("effects/"));
        tracersPool = new ParticleEffectPool(tracer, 1, 100);

        /* Enemy bullet tracers */
        ParticleEffect eTracer = new ParticleEffect();
        eTracer.load(Gdx.files.internal("effects/enemy_tracer.p"), Gdx.files.internal("effects/"));
        enemyTracersPool = new ParticleEffectPool(eTracer, 1, 100);
    }

    /** Helper function that initializes the light array and pool.
     * @param world RayHandler needs a world to light up */
    public void loadLightEffects(World world) {
        rayHandler = new RayHandler(world);
        rayHandler.setAmbientLight(1f);

        lightEffects = new Array<>();
        bgStars = new Array<>();
        lightPool = new Pool<PointLight>() {
            @Override
            protected PointLight newObject() {
                PointLight p = new PointLight(rayHandler, 128, new Color(1, 1, 1, 1), 1, 0, 0);
                p.setStaticLight(false);
                p.setSoft(true);
                return p;
            }
        };

        for (int i = 0; i < numStars; i++) {
            Random rng = new Random();
            PointLight p = lightPool.obtain();
            p.setColor(rng.nextFloat(), rng.nextFloat(), rng.nextFloat(), rng.nextFloat() * 0.5f + 0.5f);
            p.setDistance((rng.nextInt(200) + 100) * PPM);

            BodyDef bDef = new BodyDef();
            bDef.position.set(rng.nextInt((int)(JuicyShmup.GAME_WIDTH * PPM)), rng.nextInt((int)(JuicyShmup.GAME_HEIGHT * PPM)));
            bDef.type = BodyDef.BodyType.DynamicBody;
            Body b = world.createBody(bDef);
            p.attachToBody(b);

            p.getBody().setLinearVelocity(0, -rng.nextFloat() * starSpeed - 1);
            bgStars.add(p);
        }
    }

    /** Updates all effects, and removes it from the active array when finished.
     * @param deltaTime Time since last frame was called
     * @param timeMultiplier Game's game dilation multiplier */
    public void update(float deltaTime, float timeMultiplier, Camera camera, Viewport viewport) {
        for (ParticleEffectPool.PooledEffect p : effects) {
            p.update(deltaTime / timeMultiplier);
            if (p.isComplete()) {
                p.free();
                effects.removeValue(p, true);
            }
        }

        /* Shrinks light effect from explosion */
        for (PointLight p : lightEffects) {
            if (p.getDistance() > 0.01) p.setDistance(p.getDistance() - deltaTime * 10);
            else {
                lightEffects.removeValue(p, true);
                lightPool.free(p);
            }
        }

        backgroundUpdate();

        rayHandler.setCombinedMatrix(camera.combined,0,0, viewport.getScreenWidth(), viewport.getScreenHeight());
        rayHandler.update();
    }


    private void backgroundUpdate() {
        Random rng = new Random();
        for (PointLight p : bgStars) {
            if (p.getBody().getPosition().y < -p.getDistance()) {
                p.setColor(rng.nextFloat(), rng.nextFloat(), rng.nextFloat(), rng.nextFloat() * 0.5f + 0.5f);
                p.setDistance((rng.nextInt(200) + 100) * PPM);

                p.getBody().setTransform(rng.nextInt((int)(JuicyShmup.GAME_WIDTH * PPM)), JuicyShmup.GAME_HEIGHT * PPM + p.getDistance(), 0);
                p.getBody().setLinearVelocity(0, -rng.nextFloat() * starSpeed - 1);
            }
        }
    }

    /** Draws all active effects.
     * @param batch The SpriteBatch used to draw */
    public void draw(SpriteBatch batch) {
        for (PooledEffect p : effects) p.draw(batch);
    }

    /** Renders light so the world is illuminated. */
    public void renderLight() { rayHandler.render(); }

    /** Remove all active effects from the pool. */
    public void dispose() {
        for (int i = effects.size - 1; i >= 0; i--)
            effects.get(i).free();
        effects.clear();

        rayHandler.removeAll();
        rayHandler.dispose();
    }
}
