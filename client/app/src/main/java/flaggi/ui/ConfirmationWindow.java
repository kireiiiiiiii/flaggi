package flaggi.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import flaggi.common.GPanel.Interactable;
import flaggi.common.GPanel.Renderable;
import flaggi.constants.ZIndex;

public class ConfirmationWindow implements Renderable, Interactable {

    // Dimensions and styling
    private static final int DIALOG_WIDTH = 400;
    private static final int DIALOG_HEIGHT = 200;
    private static final Color BACKGROUND_COLOR = new Color(50, 50, 50, 240);
    private static final Color BORDER_COLOR = new Color(200, 200, 200);
    private static final Color BUTTON_COLOR = new Color(70, 140, 70);
    private static final Color TEXT_COLOR = Color.WHITE;

    // Dialog content
    private String question;
    private Runnable onYes;
    private Runnable onNo;

    // State
    private boolean visible = false;
    private Rectangle yesButtonBounds;
    private Rectangle noButtonBounds;

    private int dialogX, dialogY;

    public ConfirmationWindow() {
        int buttonWidth = 100;
        int buttonHeight = 40;
        int buttonY = (DIALOG_HEIGHT / 2) + 40;

        yesButtonBounds = new Rectangle((DIALOG_WIDTH / 2) - 120, buttonY, buttonWidth, buttonHeight);
        noButtonBounds = new Rectangle((DIALOG_WIDTH / 2) + 20, buttonY, buttonWidth, buttonHeight);
    }

    /**
     * Displays the confirmation dialog with the specified question and callbacks.
     *
     * @param question The question to display
     * @param onYes    The callback for the Yes button
     * @param onNo     The callback for the No button
     */
    public void show(String question, Runnable onYes, Runnable onNo) {
        this.question = question;
        this.onYes = onYes;
        this.onNo = onNo;
        this.visible = true;
    }

    @Override
    public void show() {
        this.visible = true;
    }

    /**
     * Hides the confirmation dialog.
     */
    public void hide() {
        this.visible = false;
    }

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {
        int screenWidth = size[0];
        int screenHeight = size[1];

        // Center the dialog
        this.dialogX = (screenWidth - DIALOG_WIDTH) / 2;
        this.dialogY = (screenHeight - DIALOG_HEIGHT) / 2;

        // Background
        g.setColor(BACKGROUND_COLOR);
        g.fillRoundRect(dialogX, dialogY, DIALOG_WIDTH, DIALOG_HEIGHT, 20, 20);

        // Border
        g.setColor(BORDER_COLOR);
        g.drawRoundRect(dialogX, dialogY, DIALOG_WIDTH, DIALOG_HEIGHT, 20, 20);

        // Question text
        g.setColor(TEXT_COLOR);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        int questionWidth = g.getFontMetrics().stringWidth(question);
        g.drawString(question, dialogX + (DIALOG_WIDTH - questionWidth) / 2, dialogY + DIALOG_HEIGHT / 3);

        // Yes button
        drawButton(g, dialogX + yesButtonBounds.x, dialogY + yesButtonBounds.y, yesButtonBounds.width, yesButtonBounds.height, "Yes");

        // No button
        drawButton(g, dialogX + noButtonBounds.x, dialogY + noButtonBounds.y, noButtonBounds.width, noButtonBounds.height, "No");
    }

    private void drawButton(Graphics2D g, int x, int y, int width, int height, String text) {
        // Background
        g.setColor(BUTTON_COLOR);
        g.fillRoundRect(x, y, width, height, 10, 10);

        // Border
        g.setColor(BORDER_COLOR);
        g.drawRoundRect(x, y, width, height, 10, 10);

        // Text
        g.setColor(TEXT_COLOR);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int textWidth = g.getFontMetrics().stringWidth(text);
        g.drawString(text, x + (width - textWidth) / 2, y + (height / 2) + 5);
    }

    @Override
    public boolean interact(MouseEvent e) {
        int mouseX = e.getX() - this.dialogX;
        int mouseY = e.getY() - this.dialogY;

        // Check Yes button
        if (yesButtonBounds.contains(mouseX, mouseY)) {
            if (onYes != null)
                onYes.run();
            hide();
            return true;
        }

        // Check No button
        if (noButtonBounds.contains(mouseX, mouseY)) {
            if (onNo != null)
                onNo.run();
            hide();
            return true;
        }

        return false;
    }

    @Override
    public int getZIndex() {
        return ZIndex.TOAST;
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public ArrayList<String> getTags() {
        return new ArrayList<>();
    }

}