/*
 * Author: Matěj Šťastný
 * Date created: 11/29/2024
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

package flaggiclient.ui;

import java.awt.Container;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;

import flaggiclient.common.Sprite;
import flaggiclient.constants.WidgetTags;
import flaggiclient.constants.ZIndex;
import flaggishared.GPanel.Renderable;

/**
 * Tree object.
 *
 */
public class Tree implements Renderable {

    /////////////////
    // Variables
    ////////////////

    private boolean visible;
    private int[] position;
    private Sprite sprite;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Code default constructor.
     *
     * @param position
     */
    public Tree(int[] position) {
        this.position = position;
        this.sprite = new Sprite();
        this.sprite.addAnimation(Arrays.asList("tree"), "tree");
        this.sprite.setAnimation("tree");
    }

    /////////////////
    // Rendering
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {
        this.sprite.render(g, this.position[0] + origin[0], this.position[1] + origin[1], focusCycleRootAncestor);
    }

    @Override
    public int getZIndex() {
        return ZIndex.ENVIRONMENT_TOP;
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
        ArrayList<String> tags = new ArrayList<>();
        tags.add(WidgetTags.ENVIRONMENT);
        tags.add(WidgetTags.GAME_ELEMENTS);
        return tags;
    }

}
