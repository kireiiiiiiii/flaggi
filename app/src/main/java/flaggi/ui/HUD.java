/*
 * Author: Matěj Šťastný
 * Date created: 12/7/2024
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

import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;

import flaggi.common.GPanel.Renderable;
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;
import flaggi.util.ImageUtil;

/**
 * Player HUD widget class.
 * 
 */
public class HUD implements Renderable {

    /////////////////
    // Varuables
    ////////////////

    private boolean visible;
    private float health;

    private Image healthTexture, healthFillTexture;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Default constructor for the player HUD widget.
     * 
     */
    public HUD() {
        // ---- Default variable values
        this.visible = false;
        this.health = 0;

        // ---- Textures
        this.healthTexture = ImageUtil.scaleToWidth(ImageUtil.getImageFromFile("ui/spray-hp.png"), 100, false);
        this.healthFillTexture = ImageUtil.scaleToWidth(ImageUtil.getImageFromFile("ui/spray-hp-fill.png"), 100, false);
    }

    /////////////////
    // Rendering
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {

        // ---- Calculate healthbar data
        int heightDiff = 89;
        double filledPercent = health / 100.0;
        int usableHeight = this.healthTexture.getHeight(null) - heightDiff;
        int emptyHeight = (int) (usableHeight * (1.0 - filledPercent));
        int x = 30;
        int y = size[1] - 90 - this.healthTexture.getHeight(null);
        int fillY = y + heightDiff + emptyHeight;

        // ---- Render the healthbar
        if (this.health > 0) {
            g.drawImage(ImageUtil.cropImage(this.healthFillTexture, 0, heightDiff + emptyHeight, this.healthTexture.getWidth(null), usableHeight - emptyHeight), x, fillY, focusCycleRootAncestor);
        }
        g.drawImage(this.healthTexture, x, y, focusCycleRootAncestor);

    }

    @Override
    public int getZIndex() {
        return ZIndex.HUD;
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

    /////////////////
    // Helper methods
    ////////////////

    /**
     * Sets the health data for the widget to render.
     * 
     * @param health - new health value.
     */
    public void setHealth(float health) {
        this.health = health;
    }

}