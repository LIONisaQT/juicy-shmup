package io.github.lionisaqt.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.lionisaqt.JuicyShmup;

/** Heads-up display for the game.
 * @author Ryan Shee */
public class HUD {
    /* Need a stage to draw the table to */
    public Stage stage;

    /* Need a table to put the buttons and text on */
    public Table table;

    /** Constructs a HUD.
     * @param batch The SpriteBatch for batch drawing. */
    public HUD(SpriteBatch batch) {
        Viewport viewport = new StretchViewport(JuicyShmup.GAME_WIDTH, JuicyShmup.GAME_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, batch);
        table = new Table();
        stage.addActor(table);
    }

    /** Frees stage from memory. */
    public void dispose() {
        stage.dispose();
    }
}
