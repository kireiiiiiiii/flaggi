/*
 * Author: Matěj Šťastný
 * Date created: 11/6/2024
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

package flaggi.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import flaggi.common.GPanel.Renderable;
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;
import flaggi.util.ImageUtil;

/**
 * Tileable floor texture
 *
 */
public class Floor implements Renderable {

    /////////////////
    // Variables
    /////////////////

    private boolean visible = true;
    private BufferedImage texture;

    /////////////////
    // Constructor
    /////////////////

    /**
     * Default contructor
     *
     * @param size - size of the map.
     */
    public Floor(int[] size) {
        try {
            this.texture = ImageUtil.createRepeatedImage("sprites/floor-tile.png", size[0], size[1]);
        } catch (IOException e) {
            System.out.println("There was an error while converting the floor texture.");
        }
    }

    /////////////////
    // Rendering
    /////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {
        // Floor texture
        g.drawImage(this.texture, origin[0], origin[1], focusCycleRootAncestor);

        // Border
        g.setStroke(new BasicStroke(5));
        g.setColor(Color.BLACK);
        g.drawRect(origin[0], origin[1], this.texture.getWidth(), this.texture.getHeight());
    }

    @Override
    public int getZIndex() {
        return ZIndex.FLOOR;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void hide() {
        this.visible = false;
    }

    @Override
    public void show() {
        this.visible = true;
    }

    @Override
    public ArrayList<String> getTags() {
        ArrayList<String> tags = new ArrayList<String>();
        tags.add(WidgetTags.GAME_ELEMENTS);
        return tags;
    }

}
