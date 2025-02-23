/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 11/8/2024
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
 * Widget displayed on server connection.
 *
 */
public class ConnectionWidget extends Renderable {

    private static final int RADIUS = 5;

    public ConnectionWidget() {
        super(ZIndex.CONNECTION, WidgetTags.GAME_ELEMENTS, WidgetTags.MENU_ELEMENTS);
    }

    @Override
    public void render(Graphics2D g, int[] size, int[] viewportOffset, Container focusCycleRootAncestor) {
        g.setColor(Color.GREEN);
        g.fillOval(size[0] - RADIUS * 3, RADIUS, RADIUS * 2, RADIUS * 2);
    }

}
