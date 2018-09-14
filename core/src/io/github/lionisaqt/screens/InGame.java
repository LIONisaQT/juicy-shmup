package io.github.lionisaqt.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Timer;

import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.actors.Bullet;
import io.github.lionisaqt.actors.EnemyDirector;
import io.github.lionisaqt.actors.Player;
import io.github.lionisaqt.utils.B2dContactListener;
import io.github.lionisaqt.utils.BackgroundColor;
import io.github.lionisaqt.utils.EffectsManager;
import io.github.lionisaqt.utils.TraumaManager;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;
import static io.github.lionisaqt.JuicyShmup.PPM;

/** The game screen.
 * @author Ryan Shee */
public class InGame extends MyScreen {
    private enum State { PAUSE, PLAY, RESUME }
    private State state;
    private Table pauseTable;

    private BackgroundColor backgroundColor;
    private Label scoreLabel, pauseLabel;
    private TextButton pauseButton, menuButton, resumeButton, exitButton;
    private int score = 0;

    public final EffectsManager eManager;

    public final World world;               // Box2D world
    private final Box2DDebugRenderer b2dr;  // Lets us see Box2D bodies

    public final TraumaManager tManager;    // Screen shake utility

    public Array<Bullet> bullets;           // Array of active bullets
    public Pool<Bullet> bulletPool;         // Pool of bullets

    private final EnemyDirector director;   // Controls enemy spawn

    public float timeMultiplier;            // Scales game speed

    private Player player;

    InGame(final JuicyShmup game) {
        super(game);
        state = State.PLAY;

        timeMultiplier = 1;

        tManager = new TraumaManager(camera);

        world = new World(new Vector2(0, 0), true);
        world.setContactListener(new B2dContactListener(tManager));
        b2dr = new Box2DDebugRenderer();

        eManager = new EffectsManager();
        eManager.loadParticles();
        eManager.loadLightEffects(world);

        player = new Player(game, this, JuicyShmup.GAME_WIDTH / 2 * PPM, 100 * PPM);

        final InGame iG = this;
        bullets = new Array<>();
        bulletPool = new Pool<Bullet>(1, 200) {
            @Override
            protected Bullet newObject() {
                return new Bullet(game, iG);
            }
        };

        director = new EnemyDirector(game, this);
    }

    void update(float deltaTime) {
        super.update(deltaTime);
        switch (state) {
            case PAUSE:
                pauseTable.setVisible(true);
                break;
            case RESUME:
                pauseTable.setVisible(false);
                timeMultiplier = 6f;
                state = State.PLAY;
                break;
            case PLAY:
                if (timeMultiplier != 1) normalizeGameSpeed(deltaTime);

                world.step(1 / (60f * timeMultiplier), 6, 2);

                player.update(deltaTime);
                for (Bullet b : bullets) b.update(deltaTime);
                director.update(deltaTime, player.body.getPosition());
                eManager.update(deltaTime, timeMultiplier, camera, viewport);
                tManager.manageShake(deltaTime, timeMultiplier);
                break;
            default:
                break;
        }
    }

    @Override
    void handleInput() {
        switch (Gdx.app.getType()) {
            case Android:
            case iOS:
                break;
            case Desktop:
                if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) decreaseGameSpeed();
                if (Gdx.input.isKeyJustPressed(Input.Keys.W)) increaseGameSpeed();
                if (Gdx.input.isKeyJustPressed(Input.Keys.O)) addScore(10000);
                if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                    if (pauseButton.isTouchable()) pause();
                    else resumeGame();
                }
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
            default:
                break;
        }
    }

    @Override
    void draw(SpriteBatch batch) {
        eManager.renderLight();
        batch.begin();
        player.draw(batch);
        for (Bullet b : bullets) b.draw(batch);
        director.draw(batch);
        eManager.draw(batch);
        batch.end();

        if (game.debug) b2dr.render(world, camera.combined);
    }

    /** Decreases game speed (basically slow-mo). */
    private void decreaseGameSpeed() {
        timeMultiplier += 0.5f;
        if (timeMultiplier > 5) timeMultiplier = 5f;
    }

    /** Increases game speed (basically fast-mo). */
    private void increaseGameSpeed() {
        timeMultiplier -= 0.5f;
        if (timeMultiplier < 0.5f) timeMultiplier = 0.5f;
    }

    /** Sets game speed to exact value. Max slow-mo of 5x and max fast-mo of 2x.
     * @param speed The target game speed. */
    public void setGameSpeed(float speed) {
        timeMultiplier = speed;

        /* Hard limits */
        if (timeMultiplier > 5) timeMultiplier = 5;
        if (timeMultiplier < 0.5f) timeMultiplier = 0.5f;
    }

    /** Slowly normalizes game speed.
     * TODO: Make normalization function quadratic instead of linear
     * @param deltaTime The time since last frame was called */
    private void normalizeGameSpeed(float deltaTime) {
        if (timeMultiplier > 1) timeMultiplier -= deltaTime * 3;
        else if (timeMultiplier < 1) timeMultiplier += deltaTime * 3;

        /* Resets to 1 when close enough */
        if (Math.abs(timeMultiplier - 1) < 0.1f) timeMultiplier = 1;
    }

    /** Adds score to current score, changes text. */
    public void addScore(int score) {
        this.score += score;
        scoreLabel.setText("" + this.score);
    }

    /** Returns current score.
     * @return score Player's current score. */
    public int getScore() { return score; }

    @Override
    public void dispose() {
        super.dispose();
        backgroundColor.dispose();

        world.dispose();
        b2dr.dispose();

        for (int i = bullets.size - 1; i >=0; i--)
            bullets.get(i).free();
        bullets.clear();

        director.dispose();
        eManager.dispose();
    }

    @Override
    void addUI() {
        scoreLabel = new Label("" + score, game.skin);
        scoreLabel.setFontScale(2f);

        pauseTable = new Table();
        pauseTable.setFillParent(true);
        hud.stage.addActor(pauseTable);

        pauseButton = new TextButton("||", game.skin);
        pauseButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                switch (state) {
                    case RESUME:
                    case PLAY:
                        pause();
                    default:
                        break;
                }
            }
        });

        pauseLabel = new Label("Paused", game.skin, "title");

        menuButton = new TextButton("Retreat", game.skin);
        menuButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                dispose();
                game.setScreen(new MainMenu(game));
            }
        });

        resumeButton = new TextButton("Resume", game.skin);
        resumeButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                pauseAnimationReverse();
                float delay = 1.2f;
                Timer.schedule(new Timer.Task() {
                    @Override
                    public void run() {
                        pauseButton.setTouchable(Touchable.enabled);
                        state = State.RESUME;
                    }
                }, delay);
            }
        });

        exitButton = new TextButton("Rage Quit", game.skin);
        exitButton.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                dispose();
                Gdx.app.exit();
            }
        });

        hud.table.top().left().add(pauseButton).width(75).left();
        hud.table.add().expandX();
        hud.table.right().add(scoreLabel).top();

        backgroundColor = new BackgroundColor("textures/white_color_texture.png");
        backgroundColor.setColor(130, 0, 153, 255 / 2);
        pauseTable.setBackground(backgroundColor);
        pauseTable.add(pauseLabel);
        pauseTable.row().pad(-30, 0, 0, 0);
        pauseTable.add(menuButton).minWidth(200);
        pauseTable.row();
        pauseTable.add(resumeButton).fill();
        pauseTable.setDebug(game.debug);
        pauseTable.setVisible(false);
    }

    @Override
    public void show() {
        super.show();
        scoreLabel.addAction(sequence(alpha(0),
                parallel(fadeIn(1f), moveBy(0, -10, 1f, Interpolation.pow5))));
        pauseButton.addAction(sequence(alpha(0),
                parallel(fadeIn(1f), moveBy(0, -10, 1f, Interpolation.pow5))));
    }

    /** Plays pause menu animation when pause button is clicked. */
    private void pauseAnimation() {
        pauseLabel.addAction(sequence(alpha(0),
                parallel(fadeIn(0.5f))));
        menuButton.addAction(sequence(alpha(0), delay(0.1f),
                parallel(fadeIn(2f, Interpolation.pow5), moveBy(0, -35, 1.5f, Interpolation.pow5))));
        resumeButton.addAction(sequence(alpha(0), delay(0.2f),
                parallel(fadeIn(2f, Interpolation.pow5), moveBy(0, -35, 1.5f, Interpolation.pow5))));
    }

    /** Reverses pause menu animation for next pause. */
    private void pauseAnimationReverse() {
        resumeButton.addAction(parallel(fadeOut(1f, Interpolation.pow5), moveBy(0, 35, 1.25f, Interpolation.pow5)));
        menuButton.addAction(sequence(delay(0.1f),
                parallel(fadeOut(1f, Interpolation.pow5), moveBy(0, 35, 1.25f, Interpolation.pow5))));
        pauseLabel.addAction(sequence(delay(0.2f),
                parallel(fadeOut(1f, Interpolation.pow5))));
    }

    @Override
    public void pause() {
        super.pause();
        pauseButton.setTouchable(Touchable.disabled);
        pauseAnimation();
        state = State.PAUSE;
    }

    private void resumeGame() {
        pauseAnimationReverse();
        float delay = 1.2f;
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                pauseButton.setTouchable(Touchable.enabled);
                state = State.RESUME;
            }
        }, delay);
    }
}
