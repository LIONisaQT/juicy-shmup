package io.github.lionisaqt.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import box2dLight.PointLight;
import box2dLight.RayHandler;
import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.actors.Bullet;
import io.github.lionisaqt.actors.Enemy;
import io.github.lionisaqt.actors.Player;
import io.github.lionisaqt.utils.B2dContactListener;
import io.github.lionisaqt.utils.TraumaManager;

import static io.github.lionisaqt.JuicyShmup.PPM;

/** The game screen.
 * @author Ryan Shee */
public class InGame extends MyScreen {
    public ParticleEffectPool effectPool;   // Pool of effects
    public Array<PooledEffect> effects;     // Array of active effects

    /* Enemy explosion effect */
    public ParticleEffectPool enemyDeathPool;

    /* Player shooting effect */
    public ParticleEffectPool shotPool;

    /* Player's engine effect */
    public ParticleEffectPool enginePool;

    /* Enemy's engine effect */
    public ParticleEffectPool enemyEnginePool;

    /* Tracers from player bullet */
    public ParticleEffectPool tracersPool;

    public World world;                     // Box2d world
    private Box2DDebugRenderer b2dr;        // Lets us see box2d bodies
    public RayHandler rayHandler;           // Manages lights

    public TraumaManager tManager;          // Screen shake utility

    public Array<Bullet> bullets;           // Array of active bullets
    public Pool<Bullet> bulletPool;         // Pool of bullets

    public Array<Enemy> enemies;            // Array of active enemies
    public Pool<Enemy> enemyPool;           // Pool of enemies

    private Player player;

    InGame(final JuicyShmup game) {
        super(game);

        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new B2dContactListener());
        b2dr = new Box2DDebugRenderer();
        rayHandler = new RayHandler(world);

        effects = new Array<>();
        ParticleEffect explosion = new ParticleEffect();
        explosion.load(Gdx.files.internal("effects/explosion.p"), Gdx.files.internal("effects/"));
        effectPool = new ParticleEffectPool(explosion, 1, 100);

        ParticleEffect eDeath = new ParticleEffect();
        eDeath.load(Gdx.files.internal("effects/enemy_death.p"), Gdx.files.internal("effects/"));
        enemyDeathPool = new ParticleEffectPool(eDeath, 1, 100);

        ParticleEffect pShoot = new ParticleEffect();
        pShoot.load(Gdx.files.internal("effects/muzzle_flash.p"), Gdx.files.internal("effects/"));
        shotPool = new ParticleEffectPool(pShoot, 1, 100);

        ParticleEffect engine = new ParticleEffect();
        engine.load(Gdx.files.internal("effects/engine.p"), Gdx.files.internal("effects/"));
        enginePool = new ParticleEffectPool(engine, 1, 100);

        ParticleEffect eEngine = new ParticleEffect();
        eEngine.load(Gdx.files.internal("effects/enemy_engine.p"), Gdx.files.internal("effects/"));
        enemyEnginePool = new ParticleEffectPool(eEngine, 1, 100);

        ParticleEffect tracer = new ParticleEffect();
        tracer.load(Gdx.files.internal("effects/tracer.p"), Gdx.files.internal("effects/"));
        tracersPool = new ParticleEffectPool(tracer, 1, 100);

        tManager = new TraumaManager(camera);

        final InGame iG = this;
        bullets = new Array<>();
        bulletPool = new Pool<Bullet>(1, 200) {
            @Override
            protected Bullet newObject() {
                return new Bullet(game, iG);
            }
        };

        if (player == null) player = new Player(game, this, JuicyShmup.GAME_WIDTH / 2 * PPM, 100 * PPM);

        enemies = new Array<>();
        enemyPool = new Pool<Enemy>() {
            @Override
            protected Enemy newObject() { return new Enemy(game, iG); }
        };
        for (int i = 0; i < 10; i++) {
            Enemy e = enemyPool.obtain();
            e.init();
            enemies.add(e);
        }

        PointLight pl = new PointLight(rayHandler, 128, new Color(0.2f,1,1,1f), 300 * PPM, JuicyShmup.GAME_WIDTH / 2 * PPM, JuicyShmup.GAME_HEIGHT / 2 * PPM);
        pl.setStaticLight(false);
        pl.setSoft(true);
    }

    @Override
    void addUI() {
        // Label with our screen name
        Label label = new Label(getClass().getSimpleName(), game.skin);

        // Menu button
        TextButton menu = new TextButton("Back to Menu", game.skin);
        menu.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new MainMenu(game));
                dispose();
            }
        });

        hud.table.top();
        hud.table.add(label);
        hud.table.row().pad(10, 0, 0, 0); // Next table addition will be padded
        hud.table.add(menu);
    }

    @Override
    void update(float deltaTime) {
        world.step(1/60f, 6, 2);

        player.update(deltaTime);
        for (Bullet b : bullets) b.update(deltaTime);
        for (Enemy e : enemies) e.update(deltaTime);

        /* Updates particle effects, and removes it from the active array when finished */
        for (ParticleEffectPool.PooledEffect p : effects) {
            p.update(deltaTime);
            if (p.isComplete()) {
                p.free();
                effects.removeValue(p, true);
            }
        }

        world.step(deltaTime, 6 , 2);
        rayHandler.setCombinedMatrix(camera.combined,0,0, viewport.getScreenWidth(), viewport.getScreenHeight());
        rayHandler.update();

        tManager.manageShake(deltaTime);
    }

    @Override
    void draw(SpriteBatch batch) {
        rayHandler.render();

        batch.begin();
        player.draw(batch);
        for (Enemy e : enemies) e.draw(batch);
        for (Bullet b : bullets) b.draw(batch);
        for (ParticleEffectPool.PooledEffect p : effects) p.draw(batch);
        batch.end();

        if (game.debug) b2dr.render(world, camera.combined);
    }

    @Override
    public void dispose() {
        super.dispose();
        world.dispose();
        b2dr.dispose();

        for (int i = effects.size - 1; i >= 0; i--)
            effects.get(i).free();  // Free all effects back to pool
        effects.clear();            // Clear current effects array

        for (int i = bullets.size - 1; i >=0; i--)
            bullets.get(i).free();  // Free all bullets back to pool
        bullets.clear();            // Clear current bullets array

        for (int i = enemies.size -1 ; i >= 0; i--)
            enemies.get(i).free();  // Frees all enemies back to pool
        enemies.clear();            // Clears current enemies array

        rayHandler.removeAll();
        rayHandler.dispose();
    }
}
