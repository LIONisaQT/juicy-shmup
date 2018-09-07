package io.github.lionisaqt.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.Viewport;

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
            enginePool,                     // Player engine
            enemyEnginePool,                // Enemy engine
            tracersPool;                    // Friendly bullet tracer

    public RayHandler rayHandler;
    public Array<PointLight> lightEffects;  // Holds all light effects for dying entities
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
    }

    /** Helper function that initializes the light array and pool.
     * @param world RayHandler needs a world to light up */
    public void loadLightEffects(World world) {
        rayHandler = new RayHandler(world);

        PointLight pl = new PointLight(rayHandler, 128, new Color(0.2f,1,1,1), 300 * PPM, JuicyShmup.GAME_WIDTH / 2 * PPM, JuicyShmup.GAME_HEIGHT / 2 * PPM);
        pl.setStaticLight(false);
        pl.setSoft(true);

        lightEffects = new Array<>();
        lightPool = new Pool<PointLight>() {
            @Override
            protected PointLight newObject() {
                PointLight p = new PointLight(rayHandler, 128, new Color(1, 1, 1, 1), 1, 0, 0);
                p.setStaticLight(false);
                p.setSoft(true);
                return p;
            }
        };
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
            if (p.getDistance() > 0.01) p.setDistance(p.getDistance() - deltaTime * 20);
            else {
                lightEffects.removeValue(p, true);
                lightPool.free(p);
            }
        }

        rayHandler.setCombinedMatrix(camera.combined,0,0, viewport.getScreenWidth(), viewport.getScreenHeight());
        rayHandler.update();
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
