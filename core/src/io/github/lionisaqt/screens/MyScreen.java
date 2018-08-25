package io.github.lionisaqt.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.lionisaqt.JuicyShmup;
import io.github.lionisaqt.utils.HUD;

public abstract class MyScreen implements Screen {
    JuicyShmup game;

    HUD hud;
    OrthographicCamera camera;
    Viewport viewport;

    MyScreen(final JuicyShmup game) {
        this.game = game;

        // Create and move camera to center
        camera = new OrthographicCamera();
        camera.setToOrtho(false);
        viewport = new StretchViewport(JuicyShmup.GAME_WIDTH * JuicyShmup.PPM, JuicyShmup.GAME_HEIGHT * JuicyShmup.PPM, camera);
        camera.translate(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        camera.update();

        hud = new HUD(game.batch);
        hud.table.setFillParent(true);
        hud.table.setDebug(game.debug);

        addUI();
    }

    abstract void addUI();

    @Override
    public void show() {
        Gdx.input.setInputProcessor(hud.stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.batch.setProjectionMatrix(camera.combined);

        update(delta);
        hud.stage.act();
        draw(game.batch);
        hud.stage.draw();
    }

    abstract void update(float deltaTime);

    abstract void draw(SpriteBatch batch);

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        hud.dispose();
    }
}
