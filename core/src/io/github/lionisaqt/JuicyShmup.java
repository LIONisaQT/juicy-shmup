package io.github.lionisaqt;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import io.github.lionisaqt.screens.MainMenu;
import io.github.lionisaqt.utils.Assets;

public class JuicyShmup extends Game {
	public static final float GAME_HEIGHT = 768;
	public static final float GAME_WIDTH = GAME_HEIGHT * 9/16f;
	public static final float PPM = 1/32f;

	public final Assets assets = new Assets();
	public SpriteBatch batch;

	public Skin skin;
	public boolean debug = true;

	@Override
	public void create () {
		assets.load();
		assets.manager.finishLoading(); // Moves on only when finished loading
        batch = new SpriteBatch();
		skin = assets.manager.get(assets.skin);
		this.setScreen(new MainMenu(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		skin.dispose();
		batch.dispose();
		assets.dispose();
	}
}
