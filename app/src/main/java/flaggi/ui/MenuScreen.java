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

/**
 * Menu screen for Flaggi.
 * 
 */
public class MenuScreen implements Renderable, Interactable, Typable {

    /////////////////
    // Variables
    ////////////////

    private boolean visible;

    // Button bounding boxes
    private Rectangle nameFieldBounds, ipFieldBounds, startButtonBounds;

    private String nameText = "", ipText = "";
    private boolean isNameFieldFocused = false, isIpFieldFocused = false;
    private Runnable startAction;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Default widget constructor.
     * 
     * @param startAction - {@code Runnable} to be executed when the start button is
     *                    clicked.
     */
    public MenuScreen(Runnable startAction) {
        this.visible = true;
        this.startAction = startAction;

        int fieldWidth = 200, fieldHeight = 30, buttonWidth = 100, buttonHeight = 40;
        int startX = 300, startY = 200;

        nameFieldBounds = new Rectangle(startX, startY, fieldWidth, fieldHeight);
        ipFieldBounds = new Rectangle(startX, startY + 50, fieldWidth, fieldHeight);
        startButtonBounds = new Rectangle(startX, startY + 120, buttonWidth, buttonHeight);
    }

    /////////////////
    // Rendering
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {

        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(nameFieldBounds.x, nameFieldBounds.y, nameFieldBounds.width, nameFieldBounds.height);
        g.fillRect(ipFieldBounds.x, ipFieldBounds.y, ipFieldBounds.width, ipFieldBounds.height);

        g.setColor(isNameFieldFocused ? Color.BLUE : Color.BLACK);
        g.drawString("Name: " + nameText, nameFieldBounds.x + 5, nameFieldBounds.y + 20);

        g.setColor(isIpFieldFocused ? Color.BLUE : Color.BLACK);
        g.drawString("IP: " + ipText, ipFieldBounds.x + 5, ipFieldBounds.y + 20);

        g.setColor(Color.GREEN);
        g.fillRect(startButtonBounds.x, startButtonBounds.y, startButtonBounds.width, startButtonBounds.height);
        g.setColor(Color.BLACK);
        g.drawString("Start", startButtonBounds.x + 30, startButtonBounds.y + 25);

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
            if (startAction != null) {
                startAction.run();
            }
            return true;
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
                nameText += c;
            } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && nameText.length() > 0) {
                nameText = nameText.substring(0, nameText.length() - 1);
            }
        } else if (isIpFieldFocused) {
            if (Character.isDigit(c) || c == '.') {
                ipText += c;
            } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE && ipText.length() > 0) {
                ipText = ipText.substring(0, ipText.length() - 1);
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
        return nameText == null ? "" : nameText;
    }

    public String getIP() {
        return ipText == null ? "" : ipText;
    }

}
