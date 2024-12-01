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

package flaggi.ui;

import java.awt.Container;
import java.awt.Graphics2D;
import java.util.ArrayList;

import flaggi.common.GPanel.Renderable;
import flaggi.common.Sprite;
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;

/**
 * Tree object.
 * 
 */
public class Tree implements Renderable {

    private boolean visible;
    private int[] position;
    private Sprite sprite;

    public Tree(int[] position) {
        this.position = position;
        this.sprite = new Sprite("tree");
    }

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {
        this.sprite.paint(g, this.position[0] + origin[0], this.position[1] + origin[1], focusCycleRootAncestor);
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
