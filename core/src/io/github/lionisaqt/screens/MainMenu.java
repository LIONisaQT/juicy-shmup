package io.github.lionisaqt.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import io.github.lionisaqt.JuicyShmup;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/** The main menu.
 * @author Ryan Shee */
public class MainMenu extends MyScreen {
    private Label label;
    private TextButton start, options, credits, exit;

    public MainMenu(final JuicyShmup game) {
        super(game);
    }

    @Override
    void addUI() {
        label = new Label("Juicy Shmup", game.skin, "title");

        start = new TextButton("Start", game.skin);
        start.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                game.setScreen(new InGame(game));
                dispose();
            }
        });

        options = new TextButton("Options", game.skin);

        credits = new TextButton("Credits", game.skin);

        exit = new TextButton("Exit", game.skin);
        exit.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) { return true; }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) { Gdx.app.exit(); }
        });

        hud.table.padTop(-100).add(label);
        hud.table.row().padTop(-15);
        hud.table.add(start).maxWidth(225).minHeight(60).fill();
        hud.table.row().padTop(5);
        hud.table.add(options).maxWidth(200).fill();
        hud.table.row().padTop(5);
        hud.table.add(credits).maxWidth(200).fill();
        hud.table.row().padTop(5);
        hud.table.add(exit).maxWidth(200).fill();
    }

    @Override
    public void show() {
        super.show();
        label.addAction(sequence(alpha(0), moveBy(-50, 0),
                parallel(fadeIn(1f), moveBy(50, 0, 0.5f, Interpolation.pow5))));
        start.addAction(sequence(alpha(0), delay(0.15f),
                parallel(fadeIn(2f, Interpolation.pow5), moveBy(0, -35, 1.5f, Interpolation.pow5))));
        options.addAction(sequence(alpha(0), delay(0.3f),
                parallel(fadeIn(2f, Interpolation.pow5), moveBy(0, -35, 1.5f, Interpolation.pow5))));
        credits.addAction(sequence(alpha(0), delay(0.45f),
                parallel(fadeIn(2f, Interpolation.pow5), moveBy(0, -30, 1.5f, Interpolation.pow5))));
        exit.addAction(sequence(alpha(0), delay(0.6f),
                parallel(fadeIn(2f, Interpolation.pow5), moveBy(0, -25, 1.5f, Interpolation.pow5))));
    }

    void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    void handleInput() {
        switch (Gdx.app.getType()) {
            case Android:
            case iOS:
                break;
            case Desktop:
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                game.setScreen(new InGame(game));
                dispose();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                dispose();
                Gdx.app.exit();
            }
            default:
                break;
        }
    }

    @Override
    void draw(SpriteBatch batch) { /* There's nothing to draw here yet */}
}
