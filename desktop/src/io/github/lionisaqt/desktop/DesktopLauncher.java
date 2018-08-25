package io.github.lionisaqt.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import io.github.lionisaqt.JuicyShmup;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = (int)JuicyShmup.GAME_WIDTH;
		config.height = (int)JuicyShmup.GAME_HEIGHT;
		new LwjglApplication(new JuicyShmup(), config);
	}
}
