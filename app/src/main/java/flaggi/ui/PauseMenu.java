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
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;
import flaggi.util.FontUtil;

public class PauseMenu implements Renderable, Interactable {

    private static int ARC_SIZE = 10;

    /////////////////
    // Variables
    ////////////////

    private Runnable resume, quit;
    private Rectangle quitButtonBounds, resumeButtonBounds;
    private boolean visible = false;

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
        this.resume = resume;
        this.quit = quit;

        int buttonWidth = 100, buttonHeight = 40;

        // Initialize rectangles with dummy values, they will be updated during
        // rendering
        this.quitButtonBounds = new Rectangle(0, 0, buttonWidth, buttonHeight);
        this.resumeButtonBounds = new Rectangle(0, 0, buttonWidth, buttonHeight);
    }

    /////////////////
    // Rendering
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {

        // ------ Calculate sizes
        // Calculate the center of the window
        int windowWidth = size[0];
        int windowHeight = size[1];

        // Dynamically position elements in the center
        int buttonWidth = quitButtonBounds.width;
        int buttonHeight = quitButtonBounds.height;

        int centerX = windowWidth / 2;
        int centerY = windowHeight / 2;

        // Update bounding boxes for interactable elements
        this.quitButtonBounds.setBounds(centerX - buttonWidth / 2, centerY + 30, buttonWidth, buttonHeight);
        this.resumeButtonBounds.setBounds(centerX - buttonWidth / 2, centerY - 30, buttonWidth, buttonHeight);

        // ------ Draw the pause menu
        g.setColor(Color.GRAY);
        g.fillRoundRect(size[0] / 2 - 100, size[1] / 2 + -100, 200, 200 + 30, ARC_SIZE, ARC_SIZE);
        g.setColor(Color.CYAN);
        g.fillRoundRect(this.quitButtonBounds.x, this.quitButtonBounds.y, this.quitButtonBounds.width,
                this.quitButtonBounds.height, ARC_SIZE, ARC_SIZE);
        g.fillRoundRect(this.resumeButtonBounds.x, this.resumeButtonBounds.y, this.resumeButtonBounds.width,
                this.resumeButtonBounds.height, ARC_SIZE, ARC_SIZE);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String quitText = "Quit", resumeText = "Resume";
        int[] quitTextPos = FontUtil.getCenteredPos(this.quitButtonBounds.width, this.quitButtonBounds.height,
                g.getFontMetrics(),
                quitText);
        int[] resumeTextPos = FontUtil.getCenteredPos(this.resumeButtonBounds.width, this.resumeButtonBounds.height,
                g.getFontMetrics(), resumeText);
        g.drawString(quitText, quitTextPos[0] + this.quitButtonBounds.x, quitTextPos[1] + this.quitButtonBounds.y);
        g.drawString(resumeText, resumeTextPos[0] + this.resumeButtonBounds.x,
                resumeTextPos[1] + this.resumeButtonBounds.y);
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
        this.visible = false;
    }

    @Override
    public void show() {
        this.visible = true;
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

}
