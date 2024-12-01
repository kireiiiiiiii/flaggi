/*
 * Author: Matěj Šťastný
 * Date created: 12/1/2024
 * Github link: https://github.com/kireiiiiiiii/Flaggi
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package flaggi.common;

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.File;

import flaggi.util.ImageUtil;

/**
 * Sprite class to render textures and animations.
 * 
 */
public class Sprite {

    /////////////////
    // Constants
    ////////////////

    public static final int SPRITE_SCALING = 5; // Scaling from the sprite sheet dimensions to display size.

    /////////////////
    // Variables
    ////////////////

    private Image sprite;

    /////////////////
    // Constructions
    ////////////////

    /**
     * Default constructor.
     * 
     * @param name - name of the image file of the sprite in the
     *             {@code /resources/sprites/-name-.png} folder.
     */
    public Sprite(String name) {
        this.sprite = ImageUtil.getImageFromFile("sprites" + File.separator + name + ".png");
        this.sprite = ImageUtil.scaleImage(this.sprite, this.sprite.getWidth(null) * SPRITE_SCALING,
                this.sprite.getHeight(null) * SPRITE_SCALING,
                false);
    }

    /////////////////
    // Helper methods
    ////////////////

    /**
     * Getter for the image of the sprite.
     * 
     * @return - {@code Image} object of the sprite.
     */
    public Image get() {
        return this.sprite;
    }

    /**
     * Paints the current frame of the sprite on a {@code Graphics2D}.
     * 
     * @param g                      - {@code Graphics2D} of the 2D graphics
     *                               context.
     * @param x                      - x coordinate.
     * @param y                      - y coordinate.
     * @param focusCycleRootAncestor - 2D graphics context.
     */
    public void paint(Graphics2D g, int x, int y, Container focusCycleRootAncestor) {
        g.drawImage(this.sprite, x, y, focusCycleRootAncestor);
    }

    /**
     * Scales the sprite texture to match the target size.
     * 
     * @param size - target size.
     */
    public void setSize(int[] size) {
        int x = size[0], y = size[1];
        this.sprite = ImageUtil.scaleImage(this.sprite, x, y, false);
    }

    /**
     * Scales the sprite texture to match the target width.
     * 
     * @param width - target width.
     */
    public void scaleToWidth(int width) {
        this.sprite = ImageUtil.scaleToWidth(this.sprite, width, false);
    }

    /**
     * Scales the sprite texture to match the target height.
     * 
     * @param height - target height.
     */
    public void scaleToHeight(int height) {
        this.sprite = ImageUtil.scaleToHeight(this.sprite, height, false);
    }

    /**
     * Returns the texture width of the sprite texture.
     * 
     * @return - {@code int} value of the width.
     */
    public int getWidth() {
        return this.sprite.getWidth(null);
    }

    /**
     * Returns the texture height of the sprite texture.
     * 
     * @return - {@code int} value of the height.
     */
    public int getHeight() {
        return this.sprite.getHeight(null);
    }

    // TODO - ANIMATIONS

    // play, stop, restart, set fps
    // will change frames from an arraylist of images, separate timer thread will
    // change sprite-index

}
