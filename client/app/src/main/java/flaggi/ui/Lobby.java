package flaggi.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import flaggi.common.RepeatedTask;
import flaggi.common.GPanel.Interactable;
import flaggi.common.GPanel.Renderable;
import flaggi.common.GPanel.Scrollable;
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;

/**
 * Lobby
 */
public class Lobby implements Renderable, Scrollable, Interactable {

    /////////////////
    // Constants
    ////////////////

    private static final int SCROLL_SPEED = 10;
    private static final int UPDATE_INTERVAL = 3;

    /////////////////
    // Variables
    ////////////////

    private boolean visible = false;
    private List<ClientItem> clientItems;
    private LobbyHandeler handeler;
    private int scrollOffset = 0;
    private int maxScroll = 0;

    // Dimensions
    private final int ITEM_HEIGHT = 50;
    private final int PADDING = 10;
    private final int BUTTON_WIDTH = 80;
    private final int BUTTON_HEIGHT = 30;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Default constructor.
     * 
     * @param handeler - lobby action handler.
     * 
     */
    public Lobby(LobbyHandeler handeler, Runnable update) {
        RepeatedTask task = new RepeatedTask();
        task.scheduleTask(update, UPDATE_INTERVAL, TimeUnit.SECONDS);
        this.clientItems = new ArrayList<>();
        this.handeler = handeler;
    }

    /////////////////
    // Rendering
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {
        // Background
        g.setColor(new Color(240, 240, 240)); // Light gray
        g.fillRect(0, 0, size[0], size[1]);

        updateMaxScroll(size); // Update scroll bounds
        int yOffset = scrollOffset + PADDING; // Apply scroll offset

        // Render each client item
        for (ClientItem item : clientItems) {
            renderClientItem(g, item, yOffset, size);
            yOffset += ITEM_HEIGHT;

            if (yOffset > size[1])
                break; // Stop rendering if out of bounds
        }
    }

    /**
     * Renders a single client item, including its button.
     */
    private void renderClientItem(Graphics2D g, ClientItem item, int yOffset, int[] size) {
        // Draw rectangle for client
        g.setColor(new Color(200, 200, 255)); // Light blue
        g.fillRect(10, yOffset, size[0] - 20, ITEM_HEIGHT - PADDING);

        g.setColor(Color.BLACK);
        g.drawRect(10, yOffset, size[0] - 20, ITEM_HEIGHT - PADDING);

        // Draw client name
        g.drawString(item.name, 20, yOffset + ITEM_HEIGHT / 2 - 5);

        // Draw button
        g.setColor(new Color(100, 200, 100)); // Light green button
        g.fillRect(item.buttonBounds.x, yOffset + 10, BUTTON_WIDTH, BUTTON_HEIGHT);

        g.setColor(Color.BLACK);
        g.drawRect(item.buttonBounds.x, yOffset + 10, BUTTON_WIDTH, BUTTON_HEIGHT);
        g.drawString("Join", item.buttonBounds.x + 22, yOffset + 28);

        // Update button bounds for click detection
        item.buttonBounds.y = yOffset + 10;
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
    // Interactions
    ////////////////

    @Override
    public boolean interact(MouseEvent e) {
        int mouseX = e.getX();
        int mouseY = e.getY();

        for (ClientItem item : clientItems) {
            if (item.buttonBounds.contains(mouseX, mouseY)) {
                handeler.joinToLobby(item.name, item.playerID);
                return true;
            }
        }

        return false;
    }

    /////////////////
    // Public
    ////////////////

    /**
     * Sets the clients in the lobby.
     * 
     * @param clients
     */
    public void setClients(Map<Integer, String> clients) {

        this.clientItems.clear();
        int yOffset = PADDING;

        for (Map.Entry<Integer, String> entry : clients.entrySet()) {
            int clientId = entry.getKey();
            String name = entry.getValue();

            Rectangle buttonBounds = new Rectangle(700, yOffset, BUTTON_WIDTH, BUTTON_HEIGHT);
            this.clientItems.add(new ClientItem(name, clientId, buttonBounds));
            yOffset += ITEM_HEIGHT;
        }

        int[] defaultSize = { 800, 600 };
        updateMaxScroll(defaultSize);

        if (scrollOffset < -maxScroll) {
            scrollOffset = -maxScroll;
        }
    }

    /////////////////
    // Scrolling
    ////////////////

    @Override
    public void scroll(MouseWheelEvent e) {
        int scrollAmount = e.getWheelRotation() * SCROLL_SPEED;
        scrollOffset -= scrollAmount;

        if (scrollOffset > 0) {
            scrollOffset = 0;
        } else if (scrollOffset < -maxScroll) {
            scrollOffset = -maxScroll;
        }
    }

    /**
     * Updates the maximum scroll based on the size of the window.
     * 
     * @param size
     */
    private void updateMaxScroll(int[] size) {
        int totalHeight = clientItems.size() * ITEM_HEIGHT + PADDING * 3;
        maxScroll = Math.max(0, totalHeight - size[1]);
    }

    /////////////////
    // Helper Classes
    ////////////////

    /**
     * An interface for the class handeling the client joining.
     * 
     */
    public static interface LobbyHandeler {

        /**
         * Creates a lobby request.
         * 
         * @param playerName - name of the player
         * @param playerID   - ID of the player.
         */
        public void joinToLobby(String playerName, int playerID);
    }

    /**
     * Represents a single client item with name and button bounds.
     */
    private static class ClientItem {

        // Attributes
        public String name;
        public int playerID;
        public Rectangle buttonBounds;

        // Constructor
        public ClientItem(String name, int playerID, Rectangle buttonBounds) {
            this.playerID = playerID;
            this.name = name;
            this.buttonBounds = buttonBounds;
        }
    }

}
