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
import flaggieditor.common.MapData.ObjectData;

/**
 * Map object to be rendered on the map.
 * 
 */
public class GameObject implements Renderable {

    /////////////////
    // Variables
    ////////////////

    private ObjectData data;
    private boolean visible = true;

    /////////////////
    // Contructor
    ////////////////

    /**
     * Default constructor.
     * 
     * @param data - data of the game object.
     */
    public GameObject(ObjectData data) {
        this.data = data;
    }

    /////////////////
    // Rendering
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {
        // TODO rendering logic
    }

    @Override
    public int getZIndex() {
        return 2;
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
