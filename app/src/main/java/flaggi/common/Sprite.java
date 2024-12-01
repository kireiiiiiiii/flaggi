/*
 * Author: Matěj Šťastný
 * Date created: 11/4/2024
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

    // TODO - ANIMATIONS

    // play, stop, restart, set fps
    // will change frames from an arraylist of images, separate timer thread will
    // change sprite-index

}
