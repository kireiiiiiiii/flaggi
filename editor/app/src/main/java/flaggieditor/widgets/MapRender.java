/*
 * Author: Matěj Šťastný
 * Date created: 2/2/2025
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
 *
 */

package flaggieditor.widgets;

import java.awt.Container;
import java.awt.Graphics2D;
import java.util.ArrayList;

import flaggieditor.common.GPanel.Renderable;
import flaggieditor.common.MapData;

/**
 * UI widget for rendering the main map.
 * 
 */
public class MapRender implements Renderable {

    /////////////////
    // Variables
    ////////////////

    private final int grid = 10;
    private MapData map;
    private boolean visible;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Default constructor.
     * 
     * @param map - data object of the map that is being rendered.
     */
    public MapRender(MapData map) {
        this.map = map;
        this.visible = true;
    }

    /////////////////
    // Modif & Accesors
    ////////////////

    public MapData getMap() {
        return this.map;
    }

    public void setMap(MapData map) {
        this.map = map;
    }

    /////////////////
    // Render
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {
        // Calculate the size of the map
        int mapWidth;
        int mapHeight;

        // Calculate the pos
        int maPosX;
        int maPosY;

        // Render

    }

    @Override
    public int getZIndex() {
        return 0;
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
        return tags;
    }

}