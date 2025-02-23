/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 2/20/2025
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
 * Flag environment UI widgets.
 */
public class Flag extends Renderable {

    private Sprite sprite;
    private int[] position;

    public Flag(int[] position, boolean isBlue) {
        super(ZIndex.ENVIRONMENT_BOTTOM, WidgetTags.GAME_ELEMENTS);

        this.position = position;
        this.sprite = new Sprite();
        this.sprite.addAnimation(Arrays.asList("flag-blue"), "flag-blue");
        this.sprite.addAnimation(Arrays.asList("flag-red"), "flag-red");
        this.sprite.setAnimation(isBlue ? "flag-blue" : "flag-red");
    }

    @Override
    public void render(Graphics2D g, int[] size, int[] viewportOffset, Container focusCycleRootAncestor) {
        this.sprite.render(g, this.position[0] + viewportOffset[0], this.position[1] + viewportOffset[1], focusCycleRootAncestor);
    }

}
