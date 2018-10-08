package io.github.lionisaqt.utils;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/** Asset manager. Instead of a new asset (i.e. Texture, Sound, etc.) being created, we can create
 * one here for all relevant objects to use.
 * @author Ryan Shee */
public class Assets {
    public final AssetManager manager = new AssetManager();
    public final AssetDescriptor<Skin> skin = new AssetDescriptor<>("skin/quantum-horizon-ui.json", Skin.class);
    public final AssetDescriptor<Texture> ship = new AssetDescriptor<>("textures/ship.png", Texture.class);
    public final AssetDescriptor<Texture> kamikaze = new AssetDescriptor<>("textures/kamikaze.png", Texture.class);
    public final AssetDescriptor<Texture> dogfighter = new AssetDescriptor<>("textures/dogfighter.png", Texture.class);
    public final AssetDescriptor<Texture> bullet = new AssetDescriptor<>("effects/particle.png", Texture.class);
    public final AssetDescriptor<Texture> flash = new AssetDescriptor<>("textures/flash.png", Texture.class);

    // TODO: Convert from Sound to Music and see if sound works on Android
    public final AssetDescriptor<Sound> shoot = new AssetDescriptor<>("sounds/laser2.mp3", Sound.class);
    public final AssetDescriptor<Sound> death1 = new AssetDescriptor<>("sounds/explode1.wav", Sound.class);
    public final AssetDescriptor<Sound> death2 = new AssetDescriptor<>("sounds/explode2.wav", Sound.class);
    public final AssetDescriptor<Sound> hit1 = new AssetDescriptor<>("sounds/hit1.wav", Sound.class);
    public final AssetDescriptor<Sound> kill1 = new AssetDescriptor<>("sounds/kill2.mp3", Sound.class);
    public final AssetDescriptor<Sound> hurt1 = new AssetDescriptor<>("sounds/hurt2.mp3", Sound.class);

    public final AssetDescriptor<Music> bgm1 = new AssetDescriptor<>("music/bgm1.mp3", Music.class);
    public final AssetDescriptor<Music> bgm2 = new AssetDescriptor<>("music/bgm2.mp3", Music.class);
    public final AssetDescriptor<Music> bgm3 = new AssetDescriptor<>("music/bgm3.mp3", Music.class);

    /** Loads the assets. */
    public void load() {
        manager.load(skin);
        manager.load(ship);
        manager.load(kamikaze);
        manager.load(dogfighter);
        manager.load(bullet);
        manager.load(flash);

        manager.load(shoot);
        manager.load(death1);
        manager.load(death2);
        manager.load(hit1);
        manager.load(kill1);
        manager.load(hurt1);

        manager.load(bgm1);
        manager.load(bgm2);
        manager.load(bgm3);
    }

    /** Frees manager from memory. */
    public void dispose() {
        manager.dispose();
    }
}
