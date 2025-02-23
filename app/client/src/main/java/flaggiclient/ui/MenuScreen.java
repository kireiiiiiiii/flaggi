/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 11/27/2024
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggiclient.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;

import flaggiclient.App;
import flaggiclient.constants.WidgetTags;
import flaggiclient.constants.ZIndex;
import flaggishared.common.GPanel.Interactable;
import flaggishared.common.GPanel.Renderable;
import flaggishared.common.GPanel.Typable;
import flaggishared.util.FontUtil;
import flaggishared.util.ImageUtil;

/**
 * The main menu screen widget.
 */
public class MenuScreen extends Renderable implements Interactable, Typable {

    private String nameUserInput = "", ipUserInput = "", errorMessage = "";
    private boolean isNameFieldFocused = false, isIpFieldFocused = false;
    private Rectangle nameFieldBounds, ipFieldBounds, startButtonBounds;
    private Image logo, background, textField, button;
    private Runnable startButtonAction;
    private Font font;

    // Constructor --------------------------------------------------------------

    /**
     * @param startAction - {@code Runnable} to be executed when the start button is
     *                    clicked.
     */
    public MenuScreen(Runnable startAction, String name, String ip) {
        super(ZIndex.MENU_SCREEN, WidgetTags.MENU_ELEMENTS);

        this.startButtonAction = startAction;
        this.nameUserInput = name == null ? "" : name;
        this.ipUserInput = ip == null ? "" : ip;
        try {
            this.font = FontUtil.createFontFromResource("fonts/PixelifySans-VariableFont_wght.ttf").deriveFont(Font.PLAIN, 25);
        } catch (IOException | FontFormatException e) {
            App.LOGGER.addLog("Error loading font.", e);
            this.font = new Font("Arial", Font.PLAIN, 25);
        }
        try {
            this.logo = ImageUtil.getImageFromFile("ui/logo.png");
            this.background = ImageUtil.getImageFromFile("ui/menu_screen.png");
            this.button = ImageUtil.scaleToWidth(ImageUtil.getImageFromFile("ui/button.png"), 130, false);
            this.textField = ImageUtil.scaleToHeight(ImageUtil.getImageFromFile("ui/text_field.png"), 60, false);
        } catch (IOException e) {
            App.LOGGER.addLog("Couldn't load MenuScreen textures.", e);
        }

        // Dummy values
        this.nameFieldBounds = new Rectangle(0, 0, this.textField.getWidth(null), this.textField.getHeight(null));
        this.ipFieldBounds = (Rectangle) this.nameFieldBounds.clone();
        this.startButtonBounds = new Rectangle(0, 0, this.button.getWidth(null), this.button.getHeight(null));
    }

    // Rendering ----------------------------------------------------------------

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
        this.nameFieldBounds.setBounds(centerX - fieldWidth / 2, centerY - 80, fieldWidth, fieldHeight);
        this.ipFieldBounds.setBounds(centerX - fieldWidth / 2, centerY, fieldWidth, fieldHeight);
        this.startButtonBounds.setBounds(centerX - buttonWidth / 2, centerY + 130, buttonWidth, buttonHeight);

        // Render elements
        this.background = ImageUtil.scaleToFit(this.background, size[0], size[1], false);
        g.drawImage(this.background, (size[0] - this.background.getWidth(null)) / 2, (size[1] - this.background.getHeight(null)) / 2, focusCycleRootAncestor);
        g.drawImage(ImageUtil.scaleToWidth(this.logo, 800, false), centerX - 400, centerY - 450, focusCycleRootAncestor);

        // Set font
        g.setFont(this.font);

        // Render error nessage
        synchronized (this.errorMessage) { // Acces the message sychronously, as it can be modified by the app
            g.setColor(this.errorMessage.equals("Connecting...") ? Color.GREEN : Color.RED);
            int[] errorPos = FontUtil.calculateCenteredPosition(size[0], size[1], g.getFontMetrics(), this.errorMessage);
            g.drawString(this.errorMessage, errorPos[0], errorPos[1] + 90);
        }

        g.drawImage(this.textField, nameFieldBounds.x, nameFieldBounds.y, focusCycleRootAncestor);
        g.drawImage(this.textField, ipFieldBounds.x, ipFieldBounds.y, focusCycleRootAncestor);

        int[] textFieldTextPos = FontUtil.calculateCenteredPosition(this.textField.getWidth(null), this.textField.getHeight(null), g.getFontMetrics(), "Dummy");

        g.setColor(isNameFieldFocused ? Color.BLUE : Color.WHITE);
        g.drawString("Name: " + nameUserInput, nameFieldBounds.x + 20, nameFieldBounds.y + textFieldTextPos[1]);

        g.setColor(isIpFieldFocused ? Color.BLUE : Color.WHITE);
        g.drawString("IP: " + ipUserInput, ipFieldBounds.x + 20, ipFieldBounds.y + textFieldTextPos[1]);

        g.drawImage(this.button, startButtonBounds.x, startButtonBounds.y, focusCycleRootAncestor);
        g.setColor(Color.WHITE);
        int[] startButtonTextPos = FontUtil.calculateCenteredPosition(buttonWidth, buttonHeight, g.getFontMetrics(), "START");
        g.drawString("START", startButtonBounds.x + startButtonTextPos[0], startButtonBounds.y + startButtonTextPos[1]);

    }

    // Interaction --------------------------------------------------------------

    @Override
    public boolean interact(MouseEvent e) {
        if (!this.isVisible()) {
            return false;
        }

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
                clearErrorMessage();
                startButtonAction.run();
            }
            return true;
        }
        isNameFieldFocused = false;
        isIpFieldFocused = false;
        return false;
    }

    @Override
    public void type(KeyEvent e) {
        if (!this.isVisible()) {
            return;
        }

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

    // Modifiers --------------------------------------------------------------

    public void setErrorMessage(String errorMessage) {
        synchronized (this.errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    public void clearErrorMessage() {
        synchronized (this.errorMessage) {
            this.errorMessage = "";
        }
    }

    // Accesors --------------------------------------------------------------

    public String getUsernameFieldContent() {
        return nameUserInput == null ? "" : nameUserInput;
    }

    public String getIpFieldConctent() {
        return ipUserInput == null ? "" : ipUserInput;
    }

}
