package io.github.lionisaqt.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.lionisaqt.JuicyShmup;

public class HUD {
    public Stage stage;
    private Viewport viewport;
    public Table table;

    public HUD(SpriteBatch batch) {
        viewport = new StretchViewport(JuicyShmup.GAME_WIDTH, JuicyShmup.GAME_HEIGHT, new OrthographicCamera());
        stage = new Stage(viewport, batch);

        table = new Table();
        stage.addActor(table);
    }

    public void dispose() {
        stage.dispose();
    }
}
