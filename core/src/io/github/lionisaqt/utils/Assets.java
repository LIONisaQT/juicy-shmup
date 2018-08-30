package io.github.lionisaqt.utils;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/** Asset manager. Instead of a new asset (i.e. Texture, Sound, etc.) being created, we can create
 * one here for all relevant objects to use.
 * @author Ryan Shee */
public class Assets {
    public final AssetManager manager = new AssetManager();
    public final AssetDescriptor<Skin> skin = new AssetDescriptor<>("skin/quantum-horizon-ui.json", Skin.class);
    public final AssetDescriptor<Texture> img = new AssetDescriptor<>("textures/badlogic.jpg", Texture.class);
    public final AssetDescriptor<Texture> ship = new AssetDescriptor<>("textures/ship.png", Texture.class);
    public final AssetDescriptor<Texture> bullet = new AssetDescriptor<>("effects/particle.png", Texture.class);
    public final AssetDescriptor<Sound> shoot = new AssetDescriptor<>("sounds/shoot1.wav", Sound.class);
    public final AssetDescriptor<Texture> flash = new AssetDescriptor<>("textures/flash.png", Texture.class);

    /** Loads the assets. */
    public void load() {
        manager.load(skin);
        manager.load(img);
        manager.load(ship);
        manager.load(flash);
        manager.load(shoot);
        manager.load(bullet);
    }

    /** Frees manager from memory. */
    public void dispose() {
        manager.dispose();
    }
}
