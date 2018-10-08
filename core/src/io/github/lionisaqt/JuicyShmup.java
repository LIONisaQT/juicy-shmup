package io.github.lionisaqt;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

import io.github.lionisaqt.screens.MainMenu;
import io.github.lionisaqt.utils.Assets;

/** The main game class.
 * @author Ryan Shee */
public class JuicyShmup extends Game {
	public static final float GAME_HEIGHT = 1080;
	public static final float GAME_WIDTH = GAME_HEIGHT * 9/16f;
	public static final float PPM = 1/32f;

	public static final short PLAYER_BIT = 2;
	public static final short ENEMY_BIT = 4;
	public static final short DF_BIT = 8;
	public static final short ITEM_BIT = 16;

	public final Assets assets = new Assets();
	public SpriteBatch batch;

	public Skin skin;
	public boolean debug = false;

	public Array<Music> playlist = new Array<>();
	public Music currentSong;

	@Override
	public void create() {
		assets.load();
		assets.manager.finishLoading(); // Moves on only when finished loading
        batch = new SpriteBatch();
		skin = assets.manager.get(assets.skin);

		playlist.add(assets.manager.get(assets.bgm1));
		playlist.add(assets.manager.get(assets.bgm2));
		playlist.add(assets.manager.get(assets.bgm3));
		currentSong = playlist.get(new Random().nextInt(playlist.size));
		currentSong.play();

		this.setScreen(new MainMenu(this));
	}

	@Override
	public void render() {
		if (!currentSong.isPlaying()) {
			currentSong = playlist.get(new Random().nextInt(playlist.size));
			currentSong.play();

		}
		super.render();
	}
	
	@Override
	public void dispose() {
		getScreen().dispose();
		skin.dispose();
		batch.dispose();
		assets.dispose();
	}
}
