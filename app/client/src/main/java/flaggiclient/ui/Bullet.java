/*
 * Author: Matěj Šťastný
 * Date created: 12/6/2024
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

package flaggiclient.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import flaggiclient.App;
import flaggiclient.common.Sprite;
import flaggiclient.constants.WidgetTags;
import flaggiclient.constants.ZIndex;
import flaggishared.common.GPanel.Renderable;

public class Bullet implements Renderable, Runnable {

    private static int BULLET_COUNT = 0;

    private boolean visible;
    private double[] position;
    private double[] direction;
    private Sprite sprite;
    private int velocity;
    private int decayTime;
    private boolean running;
    private Thread decayUpdateThread;
    private Runnable afterDecay;
    private String toStringMsg;
    private String bulletId;

    // For trail effect
    private List<double[]> trail; // Keeps previous positions for the trail
    private static final int TRAIL_LENGTH = 10; // Maximum number of trail segments

    /////////////////
    // Constructor
    ////////////////

    /**
     * Constructor for enemy plater objects.
     *
     * @param initialPosition - Initial position of the bullet [x, y].
     * @param targetPosition  - Target position the bullet heads to [x, y].
     * @param velocity        - Velocity in points per second.
     * @param decayTime       - Time (in ms) after which the bullet disappears.
     * @param bulletId        - given ID of the bullet.
     * @param clientId        - client ID of the owner.
     */
    public Bullet(int[] initialPosition, int[] targetPosition, int velocity, int decayTime, String bulletId) {
        this(initialPosition, targetPosition, velocity, decayTime, -1);
        this.bulletId = bulletId;
        BULLET_COUNT--;
    }

    /**
     * Bullet constructor.
     *
     * @param initialPosition - Initial position of the bullet [x, y].
     * @param targetPosition  - Target position the bullet heads to [x, y].
     * @param velocity        - Velocity in points per second.
     * @param decayTime       - Time (in ms) after which the bullet disappears.
     */
    public Bullet(int[] initialPosition, int[] targetPosition, int velocity, int decayTime, int clientId) {
        this.toStringMsg = "bullet:" + BULLET_COUNT + ":" + initialPosition[0] + "&" + initialPosition[1] + ":" + targetPosition[0] + "&" + targetPosition[1] + ":" + decayTime + ":" + velocity;
        this.bulletId = clientId + "-" + BULLET_COUNT;
        this.position = new double[] { initialPosition[0], initialPosition[1] };
        this.velocity = velocity;
        this.decayTime = decayTime;
        this.sprite = new Sprite();
        this.sprite.addAnimation(Arrays.asList("bullet"), "bullet");
        this.sprite.setAnimation("bullet");
        this.visible = true;
        this.running = true;

        // Initialize trail
        this.trail = new LinkedList<>();

        // Calculate normalized direction vector
        double dx = targetPosition[0] - initialPosition[0];
        double dy = targetPosition[1] - initialPosition[1];
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        this.direction = new double[] { dx / magnitude, dy / magnitude };

        // Start the movement thread
        this.decayUpdateThread = new Thread(this, "Bullet update thread for bullet: " + this.toStringMsg.split(":")[1]);
        this.decayUpdateThread.start();

        BULLET_COUNT++;
    }

    /////////////////
    // Rendering
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {
        // Draw the trail
        for (int i = 0; i < trail.size(); i++) {
            double[] pos = trail == null ? new double[] { 0, 0 } : trail.get(i);
            float alpha = (float) (1.0 - (i / (double) trail.size())); // Fade effect
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(new Color(255, 255, 255, (int) (alpha * 255))); // White trail
            g.fillOval((int) pos[0] + origin[0], (int) pos[1] + origin[1], 6, 6);
        }

        // Reset opacity for the sprite
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        // Rotate sprite to face direction
        double angle = Math.atan2(direction[1], direction[0]);
        AffineTransform oldTransform = g.getTransform();
        g.translate(position[0] + origin[0], position[1] + origin[1]);
        g.rotate(angle);
        this.sprite.render(g, 0, -6, focusCycleRootAncestor);
        g.setTransform(oldTransform);

        // Debug: Show hitbox if enabled
        if (App.SHOW_HITBOXES) {
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(1));
            Rectangle r = new Rectangle((int) position[0] + origin[0], (int) position[1] + origin[1], 5, 5);
            g.draw(r);
        }
    }

    @Override
    public int getZIndex() {
        return ZIndex.ENVIRONMENT_TOP;
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
        tags.add(WidgetTags.GAME_ELEMENTS);
        tags.add(WidgetTags.PROJECTILES);
        return tags;
    }

    /////////////////
    // Helpers
    ////////////////

    /**
     * Sets the after decay runnable, ran after the bullet decays. Used for removing
     * the bullet from the player bullet list in App.
     *
     * @param afterDecay - {@code Runnable} to be ran after decay.
     */
    public void setAfterDecayRunnable(Runnable afterDecay) {
        this.afterDecay = afterDecay;
    }

    @Override
    public String toString() {
        return this.toStringMsg;
    }

    /**
     * Returns the player object ID in list [bulletId, clientId].
     *
     * @return - player object ID.
     */
    public String getObjectId() {
        return this.bulletId;
    }

    /////////////////
    // Movement Logic
    ////////////////

    /**
     * Thread for updating the bullet's position and handling its lifecycle.
     */
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        long lastUpdate = System.currentTimeMillis();

        while (running) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastUpdate;

            // Update position
            if (elapsedTime > 0) {
                double delta = (elapsedTime / 1000.0) * this.velocity;
                this.position[0] += this.direction[0] * delta;
                this.position[1] += this.direction[1] * delta;

                // Add position to trail
                trail.add(0, new double[] { position[0], position[1] });
                if (trail.size() > TRAIL_LENGTH) {
                    trail.remove(trail.size() - 1);
                }

                lastUpdate = currentTime;
            }

            // Check for decay
            if (currentTime - startTime >= this.decayTime) {
                if (this.afterDecay != null) {
                    this.afterDecay.run();
                }
                this.running = false;
                this.visible = false;
            }

            try {
                Thread.sleep(16); // Approx. 60 updates per second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stops the bullet thread.
     */
    public void stop() {
        this.running = false;
    }

}
