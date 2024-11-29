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
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;
import flaggi.util.ImageUtil;

public class Tree implements Renderable {

    private boolean visible;

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {
        g.drawImage(ImageUtil.getImageFromFile("tree.png"), 40 + origin[0], 40 + origin[1], focusCycleRootAncestor);
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
