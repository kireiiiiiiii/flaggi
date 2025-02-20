/*
 * Author: Matěj Šťastný
 * Date created: 1/5/2025
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

package flaggiclient.struct;

/**
 * A read only structure class for a client.
 *
 */
public class ClientStruct {

    // ---- Struct variables
    private int x, y, id, health;
    private String displayName, animationFrame, playerObjectData;

    /**
     * Default constructor
     *
     * @param x                - X position of the client.
     * @param y                - Y position of the client.
     * @param id               - ID of the client given by the server.
     * @param health           - current health of the client.
     * @param displayName      - display name of the client.
     * @param animationName    - animation frame data.
     * @param playerObjectData - player objects data.
     */
    public ClientStruct(int x, int y, int id, int health, String displayName, String animationName, String playerObjectData) {
        this.x = x;
        this.y = y;
        this.id = id;
        this.health = health;
        this.displayName = displayName;
        this.animationFrame = animationName;
        this.playerObjectData = playerObjectData;
    }

    /**
     * Accesor for the X coordinate of the client.
     *
     * @return X coordinate of the client.
     */
    public int getX() {
        return this.x;
    }

    /**
     * Accesor for the Y coordinate of the client.
     *
     * @return Y coordinate of the client.
     */
    public int getY() {
        return this.y;
    }

    /**
     * Accesor for the ID of the client.
     *
     * @return id of the client.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Accesor for the health of the client.
     *
     * @return health of the client.
     */
    public int getHealth() {
        return this.health;
    }

    /**
     * Accesor for the display name of the client.
     *
     * @return display name of the client.
     */
    public String getName() {
        return this.displayName;
    }

    /**
     * Accesor for the animation frame of the client.
     *
     * @return animation frame data.
     */
    public String getAnimationFrame() {
        return this.animationFrame;
    }

    /**
     * Accesor for the player objects data of the client.
     *
     * @return player objects data.
     */
    public String getPlayerObjectData() {
        return this.playerObjectData;
    }

    /**
     * To String method used to get the data {@code String} to send to server.
     *
     */
    @Override
    public String toString() {
        return this.id + "," + this.x + "," + this.y + "," + this.health + "," + this.displayName + "," + this.animationFrame + "," + this.playerObjectData;
    }

}
