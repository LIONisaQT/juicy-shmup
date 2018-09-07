package io.github.lionisaqt.actors;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.screens.InGame;

public class EnemyDirector {
    private InGame screen;

    /* Handles enemy class spawn */
    private enum Difficulty {NOOB, VET, PRO, ELITE, BARON, ACE}
    private Difficulty difficulty;

    /* Calculates spawn rate */
    private int scoreDelta;

    /* Base spawn timers for various tiers of enemies */
    private final float baseNoobTimer = 1.5f;
    private final float baseVetTimer = 10f;
    private final float baseEliteTimer = 15f;
    private final float baseProTimer = 20f;
    private final float baseBaronTimer = 30f;
    private final float baseAceTimer = 60f;

    public Array<Enemy> enemies;
    public Pool<Enemy> enemyPool;

    /* Current spawn timers */
    private float noobSpawnTimer, vetSpawnTimer, proSpawnTimer, eliteSpawnTimer, baronSpawnTimer, aceSpawnTimer;

    public EnemyDirector(JuicyShmup game, InGame screen) {
        this.screen = screen;
        difficulty = Difficulty.NOOB;
        scoreDelta = 0;

        /* Setting default spawn timers for the different tiers */
        noobSpawnTimer = baseNoobTimer;
        vetSpawnTimer = baseVetTimer;
        proSpawnTimer = baseProTimer;
        eliteSpawnTimer = baseEliteTimer;
        baronSpawnTimer = baseBaronTimer;
        aceSpawnTimer = baseAceTimer;

        enemies = new Array<>();
        EnemyDirector director = this;
        enemyPool = new Pool<Enemy>() {
            @Override
            protected Enemy newObject() { return new Enemy(game, screen, director); }
        };
    }

    /** Called every frame. Handles any logic.
     * @param deltaTime Time since last frame was called */
    public void update(float deltaTime) {
        spawnEnemy(deltaTime);
        updateEnemies(deltaTime);
        updateDifficulty();
        updateDelta();
    }

    /** Spawns enemy. Calculates spawn of all tiers. */
    private void spawnEnemy(float deltaTime) {
        switch (difficulty) {
            case NOOB:
                noobSpawnTimer -= deltaTime;
                if (noobSpawnTimer <= 0) {
                    noobSpawnTimer = baseNoobTimer;
                    Enemy e = enemyPool.obtain();
                    e.init();
                    enemies.add(e);
                }
                break;
            case VET:
            case PRO:
            case ELITE:
            case BARON:
            case ACE:
            default:
                break;
        }
    }

    private void updateEnemies(float deltaTime) {
        for (Enemy e : enemies) e.update(deltaTime);
    }

    /** Updates difficulty based on raw score. Should never go down. */
    private void updateDifficulty() {
        if (screen.getScore() > 120000) difficulty = Difficulty.ACE;  // Start battlestar spawn
        if (screen.getScore() > 60000) difficulty = Difficulty.BARON; // Start carrier spawn
        if (screen.getScore() > 30000) difficulty = Difficulty.ELITE; // Start squadrons (or something else idk yet)
        if (screen.getScore() > 20000) difficulty = Difficulty.PRO;   // Start kamikaze-ing dogfighter spawn
        if (screen.getScore() > 2000) difficulty = Difficulty.VET;    // Start dogfighter spawn
    }

    /** Updates enemy spawn rate based on score delta. Kill faster --> raise score delta --> increased spawns.
     * More spawns --> get overwhelmed --> kill slower --> lower score delta --> decreased spawns. Score delta
     * should never go below base values. This means the game should theoretically always be providing sizable
     * challenge for the player. */
    private void updateDelta() {

    }

    public void draw(SpriteBatch batch) {
        for (Enemy e : enemies) e.draw(batch);
    }

    public void dispose() {
        for (int i = enemies.size -1 ; i >= 0; i--)
            enemies.get(i).free();  // Frees all enemies back to pool
        enemies.clear();            // Clears current enemies array
    }
}
