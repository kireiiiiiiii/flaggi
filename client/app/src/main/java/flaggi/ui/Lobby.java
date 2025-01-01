/*
 * Author: Matěj Šťastný
 * Date created: 12/28/2024
 * Github link: https://github.com/kireiiiiiiii/flaggi
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
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import flaggi.common.GPanel.Renderable;
import flaggi.common.GPanel.Scrollable;
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;

/**
 * Lobby
 */
public class Lobby implements Renderable, Scrollable {

    /////////////////
    // Constants
    ////////////////

    public static final int SCROLL_SPEED = 10;

    /////////////////
    // Variables
    ////////////////

    private boolean visible = false;
    private List<String> clientNames;

    // Scrolling variables
    private int scrollOffset = 0; // Current scroll position
    private int maxScroll = 0; // Maximum scroll offset
    private final int itemHeight = 50; // Height of each client rectangle (including padding)
    private final int padding = 10; // Padding between rectangles

    /////////////////
    // Constructor
    ////////////////

    /**
     * Default constructor.
     */
    public Lobby() {
        this.clientNames = new ArrayList<>();
    }

    /////////////////
    // Rendering
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {
        // Background
        g.setColor(new java.awt.Color(240, 240, 240)); // Light gray
        g.fillRect(0, 0, size[0], size[1]);

        // Calculate maximum scroll offset based on content size
        updateMaxScroll(size);

        // Draw client rectangles
        int yOffset = scrollOffset + padding; // Apply scroll offset

        for (String clientName : clientNames) {
            // Draw rectangle
            g.setColor(new java.awt.Color(200, 200, 255)); // Light blue
            g.fillRect(10, yOffset, size[0] - 20, itemHeight - padding);

            // Draw border
            g.setColor(java.awt.Color.BLACK);
            g.drawRect(10, yOffset, size[0] - 20, itemHeight - padding);

            // Draw client name
            g.setColor(java.awt.Color.BLACK);
            g.drawString(clientName, 20, yOffset + itemHeight / 2 - 5);

            // Move down to next item
            yOffset += itemHeight;

            // Stop rendering if out of bounds
            if (yOffset > size[1]) {
                break;
            }
        }
    }

    @Override
    public int getZIndex() {
        return ZIndex.MENU_SCREEN;
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
        tags.add(WidgetTags.LOBBY);
        return tags;
    }

    /////////////////
    // Public
    ////////////////

    /**
     * Sets the client name list.
     * 
     * @param clientNames - new list.
     */
    public void setClients(List<String> clientNames) {
        this.clientNames = clientNames;
        updateMaxScroll(new int[] { 800, 600 }); // Default size
    }

    /////////////////
    // Scrolling
    ////////////////

    @Override
    public void scroll(MouseWheelEvent e) {
        int scrollAmount = e.getWheelRotation() * SCROLL_SPEED;
        scrollOffset -= scrollAmount;

        // Clamp the scroll offset within bounds
        if (scrollOffset > 0) {
            scrollOffset = 0;
        } else if (scrollOffset < -maxScroll) {
            scrollOffset = -maxScroll;
        }
    }

    private void updateMaxScroll(int[] size) {
        // Calculate the total height of all elements, including padding
        int totalHeight = clientNames.size() * (itemHeight);
        totalHeight += padding * 3 + itemHeight;

        // Calculate the maximum scroll offset
        maxScroll = Math.max(0, totalHeight - size[1]);
    }
}
