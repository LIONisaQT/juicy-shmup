package io.github.lionisaqt.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
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
    private int previousScore;

    /* How often score delta is checked */
    private float checkDelta;

    /* Counts how many times scoreDelta hasn't increased enough */
    private short easyCount;

    /* Base spawn timers for various tiers of enemies */
    private float baseMultiplier;
    private float baseNoobTimer;
    private float baseVetTimer;
    private float baseEliteTimer;
    private float baseProTimer;
    private float baseBaronTimer;
    private float baseAceTimer;

    public Array<Enemy> enemies;
    public Pool<Enemy> enemyPool;
    public Pool<Dogfighter> dogfighterPool;

    /* Current spawn timers */
    private float noobSpawnTimer, vetSpawnTimer, proSpawnTimer, eliteSpawnTimer, baronSpawnTimer, aceSpawnTimer;

    /* Maximum amount of specific units allowed at any given time */
    private final int maxDF;

    /* Current number of specific unit at any given time */
    int currDF;

    public EnemyDirector(JuicyShmup game, InGame screen) {
        this.screen = screen;
        difficulty = Difficulty.NOOB;

        baseMultiplier = 1f;
        baseNoobTimer = 1f * baseMultiplier;
        baseVetTimer = 5f * baseMultiplier;
        baseEliteTimer = 15f * baseMultiplier;
        baseProTimer = 20f * baseMultiplier;
        baseBaronTimer = 30f * baseMultiplier;
        baseAceTimer = 60f * baseMultiplier;

        noobSpawnTimer = baseNoobTimer;
        vetSpawnTimer = baseVetTimer;
        proSpawnTimer = baseProTimer;
        eliteSpawnTimer = baseEliteTimer;
        baronSpawnTimer = baseBaronTimer;
        aceSpawnTimer = baseAceTimer;

        previousScore = 0;
        checkDelta = 5;
        easyCount = 3;
        maxDF = 3;
        currDF = 0;

        enemies = new Array<>();
        EnemyDirector director = this;
        enemyPool = new Pool<Enemy>() {
            @Override
            protected Enemy newObject() { return new Enemy(game, screen, director); }
        };
        dogfighterPool = new Pool<Dogfighter>() {
            @Override
            protected Dogfighter newObject() { return new Dogfighter(game, screen, director); }
        };
    }

    /** Called every frame. Handles any logic.
     * @param deltaTime Time since last frame was called
     * @param playerHp Player's current hp
     * @param playerPos Player's position */
    public void update(float deltaTime, int playerHp, Vector2 playerPos) {
        spawnEnemy(deltaTime);
        updateEnemies(deltaTime, playerHp, playerPos);
        updateDifficulty();
        updateDelta(deltaTime);
    }

    /** Spawns enemy. Calculates spawn of all tiers. */
    private void spawnEnemy(float deltaTime) {
        switch (difficulty) {
            case ACE:
            case BARON:
            case ELITE:
            case PRO:
            case VET:
                vetSpawnTimer -= deltaTime / screen.timeMultiplier;
                if (vetSpawnTimer <= 0 && currDF < maxDF) {
                    vetSpawnTimer = baseVetTimer;
                    Dogfighter d = dogfighterPool.obtain();
                    d.init();
                    enemies.add(d);
                    currDF++;
                }
            case NOOB:
                noobSpawnTimer -= deltaTime / screen.timeMultiplier;
                if (noobSpawnTimer <= 0) {
                    noobSpawnTimer = baseNoobTimer;
                    Enemy e = enemyPool.obtain();
                    e.init();
                    enemies.add(e);
                }
                break;
            default:
                break;
        }
    }

    private void updateEnemies(float deltaTime, int playerHp, Vector2 playerPos) {
        for (Enemy e : enemies) e.update(deltaTime, playerHp, playerPos);
    }

    /** Updates difficulty based on raw score. Should never go down. */
    private void updateDifficulty() {
        if (screen.getScore() > 120000) difficulty = Difficulty.ACE;        // Start battlestar spawn
        else if (screen.getScore() > 60000) difficulty = Difficulty.BARON;  // Start carrier spawn
        else if (screen.getScore() > 30000) difficulty = Difficulty.ELITE;  // Start squadrons (or something else idk yet)
        else if (screen.getScore() > 20000) difficulty = Difficulty.PRO;    // Start kamikaze-ing dogfighter spawn
        else if (screen.getScore() > 2000) difficulty = Difficulty.VET;     // Start dogfighter spawn
    }

    /** Updates enemy spawn rate based on score delta. Kill faster --> raise score delta --> increased spawns.
     * More spawns --> get overwhelmed --> kill slower --> lower score delta --> decreased spawns. This means
     * the game should theoretically always be providing sizable challenge for the player.
     * TODO: Factor in player health delta maybe?
     * TODO: Instead of timed checks, check time since score last increased. Less time --> faster spawns.
     * TODO: After certain amount of time has elapsed, reset score multiplier.
     * */
    private void updateDelta(float deltaTime) {
        checkDelta -= deltaTime;

        if (checkDelta <= 0) {
            checkDelta = 5;
            int scoreDelta = screen.getScore() - previousScore;

            if (scoreDelta > 200) {
                // Game is too easy
                Gdx.app.log(getClass().getSimpleName(), "Increasing difficulty");
                baseMultiplier /= 1.5f;
                previousScore = screen.getScore();
                if (easyCount < 3) easyCount++;
            } else {
                if (easyCount <= 0) {
                    // Game is too hard
                    Gdx.app.log(getClass().getSimpleName(), "Lowering difficulty");
                    baseMultiplier *= 1.5f;
                    previousScore = screen.getScore();
                    easyCount = 3;
                } else {
                    // Game is just right
                    Gdx.app.log(getClass().getSimpleName(), "Just right");
                    baseMultiplier /= 1.20f;
                }
                easyCount--;
            }

            Gdx.app.log(getClass().getSimpleName(), scoreDelta + ", " + baseMultiplier);
        }
    }

    /** Draws all enemies.
     * @param batch The SpriteBatch used for drawing */
    public void draw(SpriteBatch batch) {
        for (Enemy e : enemies) e.draw(batch);
    }

    /** Frees all active enemies back to pool and clears the active enemy array. */
    public void dispose() {
        for (int i = enemies.size -1 ; i >= 0; i--)
            enemies.get(i).free();  // Frees all enemies back to pool
        enemies.clear();            // Clears current enemies array
    }
}
