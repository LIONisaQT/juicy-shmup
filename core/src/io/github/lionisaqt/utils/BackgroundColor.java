package io.github.lionisaqt.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class BackgroundColor implements Drawable {

    private Float x;
    private Float y;
    private Float width;
    private Float height;

    private Boolean fillParent;

    private String filename;
    private Texture texture;
    private TextureRegion textureRegion;
    private Sprite sprite;
    private Color color;

    public BackgroundColor(String filename) {
        this(filename, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    public BackgroundColor(String filename, float x, float y) {
        this(filename, x, y, 0.0f, 0.0f);
    }

    public BackgroundColor(String filename, float x, float y, float width, float height) {
        this.setPosition(x, y);
        this.setSize(width, height);
        initialize(filename);
    }

    private void initialize(String filename) {
        setFilename(filename);
        if (x == null || y == null)
            setPosition();	// x = 0.0f; y = 0.0f;
        if (width == null || height == null || width < 0.0f || height < 0.0f)
            setSize();	// width = 0.0f; height = 0.0f;
        if (color == null)
            setColor(255, 255, 255, 255);
        if (sprite == null) {
            try {
                setSprite();
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        if (fillParent == null)
            fillParent = true;
    }

    private void setTexture() {
        if (texture != null)
            texture.dispose();
        texture = new Texture(Gdx.files.internal(getFilename()));
    }

    private void setTextureRegion() {
        textureRegion = new TextureRegion(texture, (int) getWidth(), (int) getHeight());
    }

    private void setSprite() {
        if (texture == null)
            setTexture();
        setTextureRegion();
        sprite = new Sprite(textureRegion);
        setSpriteColor();
    }

    private void setSpriteColor() {
        sprite.setColor(color.r, color.g, color.b, color.a);
    }

    private void setPosition() {
        this.x = 0.0f;
        this.y = 0.0f;
    }

    private void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    private void setSize() {
        this.width = sprite.getWidth();
        this.height = sprite.getHeight();
    }

    private void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void setColor(int r, int g, int b, int a) {
        color = new Color(r/255f, g/255f, b/255f, a/255f);
    }

    public void setColor(float r, float g, float b, float a) {
        color = new Color(r/255f, g/255f, b/255f, a/255f);
    }

    private void setSpritePosition(float x, float y) {
        sprite.setX(x);
        sprite.setY(y);
    }

    private void updateSprite(float x, float y, float width, float height) {
        if (fillParent) {
            setSpritePosition(x, y);
            if (width != textureRegion.getRegionWidth() ||
                    height != textureRegion.getRegionHeight()) {
                setSize(width, height);
                setSprite();
            }
        }
    }

    @Override
    public void draw(Batch batch, float x, float y, float width, float height) {
        updateSprite(x, y, width, height);
        sprite.draw(batch);
    }

    @Override
    public float getLeftWidth() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setLeftWidth(float leftWidth) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getRightWidth() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setRightWidth(float rightWidth) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getTopHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setTopHeight(float topHeight) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getBottomHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setBottomHeight(float bottomHeight) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getMinWidth() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setMinWidth(float minWidth) {
        // TODO Auto-generated method stub

    }

    @Override
    public float getMinHeight() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setMinHeight(float minHeight) {
        // TODO Auto-generated method stub

    }

    private void setFilename(String filename) {
        this.filename = filename;
    }

    private String getFilename() {
        return filename;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public Boolean getFillParent() {
        return fillParent;
    }

    public void setFillParent(Boolean fillParent) {
        this.fillParent = fillParent;
    }

    public void dispose() {
        sprite.getTexture().dispose();
        textureRegion.getTexture().dispose();
        texture.dispose();
    }
}
