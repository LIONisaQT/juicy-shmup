package io.github.lionisaqt.screens;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

import io.github.lionisaqt.JuicyShmup;

public class MainMenu extends MyScreen {

    public MainMenu(final JuicyShmup game) {
        super(game);
    }

    @Override
    void addUI() {
        // Label with our screen name
        Label label = new Label(getClass().getSimpleName(), game.skin);

        // Start button
        TextButton start = new TextButton("Start", game.skin);
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

        hud.table.add(label);
        hud.table.row().pad(10, 0, 0, 0); // Next table addition will be padded
        hud.table.add(start);
    }

    @Override
    void update(float deltaTime) {}

    @Override
    void draw(SpriteBatch batch) {}

    @Override
    public void dispose() {
        super.dispose();
    }
}
