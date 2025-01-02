/*
 * Author: Matěj Šťastný
 * Date created: 1/1/2025
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

package flaggiserver.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import flaggiserver.App;

/**
 * Bullet class, as an player created object.
 * 
 */
public class Bullet implements Runnable {

    /////////////////
    // Constants
    ////////////////

    private final int BULLET_ID, PLAYER_ID, DECAY_TIME;
    private final int[] INITIAL_POSITION, DIRECTION_POSITION;
    private final List<Integer> RECIEVED_CLIENTS; // List of clients that the bullet creation data was send to.

    /////////////////
    // Variables
    ////////////////

    private double[] position, direction;
    private int velocity;
    private boolean isDecayThreadRunning;
    private Thread decayUpdateThread;
    private Runnable doAfterDecay;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Bullet constructor.
     *
     * @param initialPosition - Initial position of the bullet [x, y].
     * @param targetPosition  - Target position the bullet heads to [x, y].
     * @param velocity        - Velocity in points per second.
     * @param decayTime       - Time (in ms) after which the bullet disappears.
     * @param playerId        - ID of the player who created the bullet.
     * @param bulletNum       - Unique identifier for the bullet.
     * @param afterDecay      - Runnable to be executed after the bullet timer is
     *                        finished.
     */
    public Bullet(int[] initialPosition, int[] targetPosition, int velocity, int decayTime, int playerId, int bulletNum) {

        this.RECIEVED_CLIENTS = new ArrayList<Integer>();
        this.PLAYER_ID = playerId;
        this.BULLET_ID = bulletNum;
        this.INITIAL_POSITION = initialPosition;
        this.DIRECTION_POSITION = targetPosition;
        this.DECAY_TIME = decayTime;

        Runnable afterDecay = () -> {
            synchronized (App.clients) {
                Iterator<ClientStruct> iterator = App.clients.iterator();
                while (iterator.hasNext()) {
                    ClientStruct c = iterator.next();
                    if (c.getID() == this.PLAYER_ID) {
                        c.removePlayerObject(this);
                        break;
                    }
                }
            }
            App.playerObjects.remove(this);
        };

        this.velocity = velocity;
        this.isDecayThreadRunning = true;
        this.doAfterDecay = afterDecay;

        this.position = new double[] { initialPosition[0], initialPosition[1] };

        // Calculate normalized direction vector
        double dx = targetPosition[0] - initialPosition[0];
        double dy = targetPosition[1] - initialPosition[1];
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        this.direction = new double[] { dx / magnitude, dy / magnitude };

        // Start the movement thread
        this.decayUpdateThread = new Thread(this, "Bullet decay update thread for bullet " + BULLET_ID + "of player " + playerId);
        this.decayUpdateThread.start();
    }

    /////////////////
    // Accesors & modifiers
    ////////////////

    /**
     * Returns the unique identifier for the bullet.
     * 
     * @return {@code int} of the unique identifier for the bullet.
     */
    public int getBulletId() {
        return BULLET_ID;
    }

    /**
     * Returns the current bullet position.
     * 
     * @return - {@code double[]} of the current bullet position in form of X, Y.
     */
    public double[] getBulletPosition() {
        return this.position;
    }

    /**
     * Checks of the client that the bullet creation data was sent to.
     * 
     * @param id - target client ID.
     */
    public void setWasSendToClient(int id) {
        this.RECIEVED_CLIENTS.add(id);
    }

    /**
     * Checks if the bullet creation data was sent to a specific client.
     * 
     * @param id - the ID of the target client.
     */
    public boolean wasCreationDataSendToClient(int id) {
        return this.RECIEVED_CLIENTS.contains(id);
    }

    /**
     * Returns the ID of the player owning this bullet object.
     * 
     * @return - {@code int} of the ID of the player owning this bullet object.
     */
    public int getOwningPlaterId() {
        return this.PLAYER_ID;
    }

    /**
     * Returns the {@code String} of the bullet creation data to be send to clients.
     * 
     */
    @Override
    public String toString() {
        return "bullet:" + PLAYER_ID + "-" + BULLET_ID + ":" + INITIAL_POSITION[0] + "&" + INITIAL_POSITION[1] + ":" + DIRECTION_POSITION[0] + "&" + DIRECTION_POSITION[1] + ":" + DECAY_TIME + ":" + velocity;
    }

    /**
     * Get the rectangle hitbox of the bullet.
     * 
     * @return
     */
    public Rectangle getHitbox() {
        return new Rectangle((int) this.position[0], (int) this.position[1], 5, 5);
    }

    /////////////////
    // Movement Logic
    ////////////////

    /**
     * Thread for updating the bullet's position and handling its lifecycle.
     * 
     */
    @Override
    public void run() {

        long startTime = System.currentTimeMillis();
        long lastUpdate = System.currentTimeMillis();

        while (isDecayThreadRunning) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastUpdate;

            // Update position based on velocity and elapsed time
            if (elapsedTime > 0) {
                double delta = (elapsedTime / 1000.0) * this.velocity; // Distance to move in this time slice
                this.position[0] += this.direction[0] * delta;
                this.position[1] += this.direction[1] * delta;
                lastUpdate = currentTime;
            }

            // Check for decay
            if (currentTime - startTime >= this.DECAY_TIME) {
                this.doAfterDecay.run();
                this.isDecayThreadRunning = false;
            }

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Stops the bullet thread.
     */
    public void stop() {
        this.isDecayThreadRunning = false;
    }

}
