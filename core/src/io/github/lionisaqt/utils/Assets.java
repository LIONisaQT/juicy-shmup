package io.github.lionisaqt.utils;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class Assets {
    public final AssetManager manager = new AssetManager();
    public final AssetDescriptor<Skin> skin = new AssetDescriptor<Skin>("skin/quantum-horizon-ui.json", Skin.class);
    public final AssetDescriptor<Texture> img = new AssetDescriptor<Texture>("textures/badlogic.jpg", Texture.class);
    public final AssetDescriptor<Texture> ship = new AssetDescriptor<Texture>("textures/ship.png", Texture.class);
    public final AssetDescriptor<Texture> bullet = new AssetDescriptor<Texture>("effects/particle.png", Texture.class);
    public final AssetDescriptor<Sound> shoot = new AssetDescriptor<Sound>("sounds/shoot1.wav", Sound.class);

    public void load() {
        manager.load(skin);
        manager.load(img);
        manager.load(ship);
        manager.load(shoot);
        manager.load(bullet);
    }

    public void dispose() {
        manager.dispose();
    }
}
