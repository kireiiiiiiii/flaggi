/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 11/6/2024
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggiclient.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;

import flaggiclient.constants.WidgetTags;
import flaggiclient.constants.ZIndex;
import flaggishared.common.GPanel.Renderable;

/**
 * Background widget.
 */
public class Background extends Renderable {

    public Background() {
        super(ZIndex.BACKGROUND, WidgetTags.MENU_ELEMENTS, WidgetTags.GAME_ELEMENTS);
    }

    @Override
    public void render(Graphics2D g, int[] size, int[] viewportOffset, Container focusCycleRootAncestor) {
        g.setColor(new Color(153, 192, 255));
        g.fillRect(0, 0, size[0], size[1]);
    }

}
