/*
 * Author: Matěj Šťastný
 * Date created: 11/8/2024
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

package kireiiiiiiii.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.util.ArrayList;

import kireiiiiiiii.common.GPanel.Renderable;
import kireiiiiiiii.constants.WidgetTags;
import kireiiiiiiii.constants.ZIndex;

/**
 * Widget displayed on server connection.
 * 
 */
public class ConnectionWidget implements Renderable {

    /////////////////
    // Constants
    ////////////////

    private final int RADIUS = 5;

    /////////////////
    // Variables
    ////////////////

    private boolean visible = true;

    /////////////////
    // Rendering
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] offset, Container focusCycleRootAncestor) {
        g.setColor(Color.GREEN);
        g.fillOval(size[0] - RADIUS * 3, RADIUS, this.RADIUS * 2, this.RADIUS * 2);
    }

    @Override
    public int getZIndex() {
        return ZIndex.CONNECTION;
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
        tags.add(WidgetTags.MENU_ELEMENTS);
        return tags;
    }

}
