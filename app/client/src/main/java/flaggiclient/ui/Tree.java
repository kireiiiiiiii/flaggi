/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 11/29/2024
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggiclient.ui;

import java.awt.Container;
import java.awt.Graphics2D;
import java.util.Arrays;

import flaggiclient.common.Sprite;
import flaggiclient.constants.WidgetTags;
import flaggiclient.constants.ZIndex;
import flaggishared.common.GPanel.Renderable;

/**
 * Tree game object widget.
 */
public class Tree extends Renderable {

    private int[] position;
    private Sprite sprite;

    public Tree(int[] position) {
        super(ZIndex.ENVIRONMENT_BOTTOM, WidgetTags.GAME_ELEMENTS, WidgetTags.ENVIRONMENT);
        this.position = position;
        this.sprite = new Sprite();
        this.sprite.addAnimation(Arrays.asList("tree"), "tree");
        this.sprite.setAnimation("tree");
    }

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {
        this.sprite.render(g, this.position[0] + origin[0], this.position[1] + origin[1], focusCycleRootAncestor);
    }

}
