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

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.util.ArrayList;

import flaggi.common.GPanel.Renderable;
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;

/**
 * Background widget.
 * 
 */
public class Background implements Renderable {

    /////////////////
    // Variables
    ////////////////

    private boolean visible = true;

    /////////////////
    // Rendering
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] offset, Container focusCycleRootAncestor) {
        g.setColor(new Color(229, 204, 255));
        g.fillRect(0, 0, size[0], size[1]);
    }

    @Override
    public int getZIndex() {
        return ZIndex.BACKGROUND;
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
        tags.add(WidgetTags.MENU_ELEMENTS);
        tags.add(WidgetTags.GAME_ELEMENTS);
        return tags;
    }

}
