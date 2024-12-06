/*
 * Author: Matěj Šťastný
 * Date created: 11/28/2024
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

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import flaggi.common.GPanel.Interactable;
import flaggi.common.GPanel.Renderable;
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;
import flaggi.util.FontUtil;

/**
 * Pause menu screen UI with fade-in and fade-out animation. Contains 2 buttons:
 * "Quit" and "Resume".
 * 
 */
public class PauseMenu implements Renderable, Interactable {

    /////////////////
    // Constants
    ////////////////

    private static final int FADE_DURATION_MS = 100;

    /////////////////
    // Variables
    ////////////////

    private long fadeStartTime = 0;
    private boolean fadingIn, isPaused, visible;
    private Runnable resume, quit;
    private Rectangle quitButtonBounds, resumeButtonBounds;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Default constructor.
     * 
     * @param resume - resume button action
     * @param quit   - quit button action
     */
    public PauseMenu(Runnable resume, Runnable quit) {

        // ----- Variable init
        this.resume = resume;
        this.quit = quit;
        this.fadingIn = false;
        this.isPaused = false;
        this.visible = true;
        int buttonWidth = 100, buttonHeight = 40;

        // Dummy values, real values are put in during rendering there is acces to
        // window size.
        this.quitButtonBounds = new Rectangle(0, 0, buttonWidth, buttonHeight);
        this.resumeButtonBounds = new Rectangle(0, 0, buttonWidth, buttonHeight);
    }

    /////////////////
    // Fade Animation
    ////////////////

    /**
     * Starts fade-in or fade-out animation.
     *
     * @param fadeIn - True for fade-in, false for fade-out.
     */
    public void startFade(boolean fadeIn) {
        this.fadeStartTime = System.currentTimeMillis();
        this.fadingIn = fadeIn;

        if (fadeIn) {
            this.isPaused = true; // Menu active during fade-in
            this.visible = true; // Ensure menu is visible
        }
    }

    /**
     * Calculates the fade progress (0 to 1).
     * 
     * @return - A float representing the fade progress.
     */
    private float getFadeProgress() {
        long elapsed = System.currentTimeMillis() - fadeStartTime;
        float progress = Math.min(elapsed / (float) FADE_DURATION_MS, 1.0f);
        return fadingIn ? progress : 1.0f - progress;
    }

    /////////////////
    // Rendering
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {

        // Dynamically position elements in the center
        int buttonWidth = quitButtonBounds.width;
        int buttonHeight = quitButtonBounds.height;

        int centerX = size[0] / 2;
        int centerY = size[1] / 2;

        // Update bounding boxes for interactable elements
        this.quitButtonBounds.setBounds(centerX - buttonWidth / 2, centerY + 40, buttonWidth, buttonHeight);
        this.resumeButtonBounds.setBounds(centerX - buttonWidth / 2, centerY - 10, buttonWidth, buttonHeight);

        // Calculate fade progress
        float fadeProgress = getFadeProgress();

        // Handle fade-out completion
        if (!fadingIn && fadeProgress <= 0) {
            this.isPaused = false; // Fully close the menu
            this.visible = false; // Hide it from rendering
            return;
        }

        // ------ Background overlay with fade
        int alpha = (int) (fadeProgress * 150); // Max alpha = 150
        g.setColor(new Color(0, 0, 0, alpha));
        g.fillRect(0, 0, size[0], size[1]);

        if (isPaused || fadingIn) {
            // ------ Pause menu box
            g.setColor(new Color(50, 50, 50, (int) (fadeProgress * 220)));
            int menuWidth = 300;
            int menuHeight = 250;
            int menuX = centerX - menuWidth / 2;
            int menuY = centerY - menuHeight / 2;
            g.fillRoundRect(menuX, menuY, menuWidth, menuHeight, 30, 30);

            // ------ Title text
            g.setFont(new Font("Arial", Font.BOLD, 28));
            g.setColor(new Color(255, 255, 255, (int) (fadeProgress * 255)));
            String titleText = "Paused";
            int[] titlePos = FontUtil.getCenteredPos(menuWidth, 50, g.getFontMetrics(), titleText);
            g.drawString(titleText, titlePos[0] + menuX, titlePos[1] + menuY + 40);

            // ------ Draw buttons
            drawButton(g, "Resume", this.resumeButtonBounds, new Color(0, 153, 255), Color.WHITE, fadeProgress);
            drawButton(g, "Quit", this.quitButtonBounds, new Color(255, 77, 77), Color.WHITE, fadeProgress);
        }
    }

    /**
     * Draws a button with a gradient and fade effect.
     *
     * @param g            - Graphics2D instance.
     * @param text         - Button text.
     * @param bounds       - Button bounds (rectangle).
     * @param baseColor    - Base color for the gradient.
     * @param textColor    - Color of the button text.
     * @param fadeProgress - Progress of the fade animation.
     */
    private void drawButton(Graphics2D g, String text, Rectangle bounds, Color baseColor, Color textColor, float fadeProgress) {
        GradientPaint gradient = new GradientPaint(bounds.x, bounds.y, baseColor.brighter(), bounds.x, bounds.y + bounds.height, baseColor.darker());
        g.setPaint(gradient);
        int alpha = (int) (fadeProgress * 255);
        g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha));
        g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);

        g.setColor(new Color(baseColor.darker().darker().getRed(), baseColor.darker().darker().getGreen(), baseColor.darker().darker().getBlue(), alpha));
        g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), alpha));
        int[] textPos = FontUtil.getCenteredPos(bounds.width, bounds.height, g.getFontMetrics(), text);
        g.drawString(text, bounds.x + textPos[0], bounds.y + textPos[1]);
    }

    @Override
    public int getZIndex() {
        return ZIndex.PAUSE_SCREEN;
    }

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void hide() {
        if (isPaused) { // Prevent fade-out unless already shown
            startFade(false); // Start fade-out
        }
    }

    @Override
    public void show() {
        if (!isPaused) { // Prevent re-triggering fade-in
            startFade(true); // Start fade-in
        }
    }

    @Override
    public ArrayList<String> getTags() {
        ArrayList<String> tags = new ArrayList<>();
        tags.add(WidgetTags.PAUSE_MENU);
        return tags;
    }

    /////////////////
    // Interaction
    ////////////////

    @Override
    public boolean interact(MouseEvent e) {
        if (!visible)
            return false;

        if (this.quitButtonBounds.contains(e.getPoint())) {
            if (quit != null) {
                quit.run();
            }
            return true;
        } else if (this.resumeButtonBounds.contains(e.getPoint())) {
            if (resume != null) {
                resume.run();
            }
            return true;
        }
        return false;
    }

    public void setPause(boolean pause) {
        this.isPaused = pause;
    }

}
