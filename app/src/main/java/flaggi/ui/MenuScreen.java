/*
 * Author: Matěj Šťastný
 * Date created: 11/27/2024
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

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import flaggi.common.GPanel.Interactable;
import flaggi.common.GPanel.Renderable;
import flaggi.common.GPanel.Typable;
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;
import flaggi.util.ImageUtil;

/**
 * The main menu screen for Flaggi.
 * 
 */
public class MenuScreen implements Renderable, Interactable, Typable {

    /////////////////
    // Variables
    ////////////////

    private boolean visible;

    // Button bounding boxes
    private Rectangle nameFieldBounds, ipFieldBounds, startButtonBounds, exitButtonBounds;

    private String nameUserInput = "", ipUserInput = "";
    private boolean isNameFieldFocused = false, isIpFieldFocused = false;
    private Runnable startButtonAction, exitAction;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Default widget constructor.
     * 
     * @param startAction - {@code Runnable} to be executed when the start button is
     *                    clicked.
     */
    public MenuScreen(Runnable startAction, Runnable exitAction) {

        // Set variables
        this.visible = true;
        this.startButtonAction = startAction;
        this.exitAction = exitAction;

        int fieldWidth = 200, fieldHeight = 30, buttonWidth = 100, buttonHeight = 40;

        // Initialize rectangles with dummy values, they will be updated during
        // rendering
        this.nameFieldBounds = new Rectangle(0, 0, fieldWidth, fieldHeight);
        this.ipFieldBounds = new Rectangle(0, 0, fieldWidth, fieldHeight);
        this.startButtonBounds = new Rectangle(0, 0, buttonWidth, buttonHeight);
        this.exitButtonBounds = new Rectangle(0, 0, buttonWidth, buttonWidth);
    }

    /////////////////
    // Rendering
    /////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {

        // Calculate the center of the window
        int windowWidth = size[0];
        int windowHeight = size[1];

        // Dynamically position elements in the center
        int fieldWidth = nameFieldBounds.width;
        int fieldHeight = nameFieldBounds.height;
        int buttonWidth = startButtonBounds.width;
        int buttonHeight = startButtonBounds.height;

        int centerX = windowWidth / 2;
        int centerY = windowHeight / 2;

        // Update bounding boxes for interactable elements
        this.nameFieldBounds.setBounds(centerX - fieldWidth / 2, centerY - 60, fieldWidth, fieldHeight);
        this.ipFieldBounds.setBounds(centerX - fieldWidth / 2, centerY, fieldWidth, fieldHeight);
        this.startButtonBounds.setBounds(centerX - buttonWidth / 2, centerY + 60, buttonWidth, buttonHeight);
        this.exitButtonBounds.setBounds(10, 10, buttonWidth / 3, buttonWidth / 3);

        // Render elements
        g.drawImage(ImageUtil.scaleToWidth(ImageUtil.getImageFromFile("logo.png"), 600), centerX - 300, centerY - 400,
                focusCycleRootAncestor);

        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(nameFieldBounds.x, nameFieldBounds.y, nameFieldBounds.width, nameFieldBounds.height);
        g.fillRect(ipFieldBounds.x, ipFieldBounds.y, ipFieldBounds.width, ipFieldBounds.height);

        g.setColor(isNameFieldFocused ? Color.BLUE : Color.BLACK);
        g.drawString("Name: " + nameUserInput, nameFieldBounds.x + 5, nameFieldBounds.y + 20);

        g.setColor(isIpFieldFocused ? Color.BLUE : Color.BLACK);
        g.drawString("IP: " + ipUserInput, ipFieldBounds.x + 5, ipFieldBounds.y + 20);

        g.setColor(Color.GREEN);
        g.fillRect(startButtonBounds.x, startButtonBounds.y, startButtonBounds.width, startButtonBounds.height);
        g.setColor(Color.RED);
        g.fillRoundRect(exitButtonBounds.x, exitButtonBounds.y, exitButtonBounds.width, exitButtonBounds.height, 20,
                20);
        g.setColor(Color.BLACK);
        g.drawString("Start", startButtonBounds.x + (buttonWidth / 2) - 20,
                startButtonBounds.y + (buttonHeight / 2) + 5);

    }

    @Override
    public int getZIndex() {
        return ZIndex.MENU_SCREEN;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void hide() {
        visible = false;
    }

    @Override
    public void show() {
        visible = true;
    }

    @Override
    public ArrayList<String> getTags() {
        ArrayList<String> tags = new ArrayList<>();
        tags.add(WidgetTags.MENU_ELEMENTS);
        return tags;
    }

    /////////////////
    // Interactions
    ////////////////

    @Override
    public boolean interact(MouseEvent e) {
        if (!visible)
            return false;

        if (nameFieldBounds.contains(e.getPoint())) {
            isNameFieldFocused = true;
            isIpFieldFocused = false;
            return true;
        } else if (ipFieldBounds.contains(e.getPoint())) {
            isIpFieldFocused = true;
            isNameFieldFocused = false;
            return true;
        } else if (startButtonBounds.contains(e.getPoint())) {
            if (startButtonAction != null) {
                startButtonAction.run();
            }
            return true;
        } else if (exitButtonBounds.contains(e.getPoint())) {
            if (exitAction != null) {
                exitAction.run();
            }
        }
        isNameFieldFocused = false;
        isIpFieldFocused = false;
        return false;
    }

    @Override
    public void type(KeyEvent e) {
        if (!visible)
            return;

        char c = e.getKeyChar();
        if (isNameFieldFocused) {
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c)) {
                nameUserInput += c;
            } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && nameUserInput.length() > 0) {
                nameUserInput = nameUserInput.substring(0, nameUserInput.length() - 1);
            }
        } else if (isIpFieldFocused) {
            if (Character.isDigit(c) || c == '.') {
                ipUserInput += c;
            } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && ipUserInput.length() > 0) {
                ipUserInput = ipUserInput.substring(0, ipUserInput.length() - 1);
            }
        }
    }

    /////////////////
    // Accesors
    ////////////////

    /**
     * Returns the entered user name.
     * 
     * @return
     */
    public String getName() {
        return nameUserInput == null ? "" : nameUserInput;
    }

    public String getIP() {
        return ipUserInput == null ? "" : ipUserInput;
    }

}
