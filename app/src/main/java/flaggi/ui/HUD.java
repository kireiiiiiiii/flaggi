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

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;

import flaggi.common.GPanel.Renderable;
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;

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

    /////////////////
    // Constructor
    ////////////////

    public HUD() {
        this.visible = false;
        this.health = 0;
    }

    /////////////////
    // Rendering
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {

        int barWidth = 300;
        int barHeight = 30;
        int x = 30;
        int y = size[1] - 90 - barHeight;

        // Calculate health bar fill
        int filledWidth = (int) (barWidth * (health / 100));

        // Draw the background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, barWidth, barHeight);

        // Draw the filled health bar
        g.setColor(Color.RED);
        g.fillRect(x, y, filledWidth, barHeight);

        // Draw the border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, barWidth, barHeight);

        // Label
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        String healthText = "HP: " + (int) this.health + "%";
        int textWidth = g.getFontMetrics().stringWidth(healthText);
        int textHeight = g.getFontMetrics().getHeight();
        g.drawString(healthText, x + (barWidth - textWidth) / 2, y + (barHeight + textHeight) / 2 - 4);
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
     * @param health - new data.
     */
    public void setHealth(float health) {
        this.health = health;
    }

}