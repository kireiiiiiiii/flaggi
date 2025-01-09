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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Structure for a client object.
 * 
 */
public class ClientStruct {

    private final int ID;
    private final String DISPLAY_NAME;
    private final InetAddress INET_ADRESS;

    private String animationFrame;
    private int x, y, health, roomID;
    private long lastReceivedTime;
    private List<Bullet> playerObjects;

    /**
     * Default constructor
     * 
     * @param id          - server given player ID.
     * @param displayName - user selected ID.
     * @param inetAddress - client address.
     */
    public ClientStruct(int id, String displayName, InetAddress inetAddress) {
        this.playerObjects = new ArrayList<Bullet>();
        this.roomID = -1;
        this.ID = id;
        this.DISPLAY_NAME = displayName;
        this.INET_ADRESS = inetAddress;
        updateLastReceivedTime();
    }

    public int getID() {
        return ID;
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public InetAddress getINET_ADRESS() {
        return INET_ADRESS;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHealth() {
        return this.health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getRoomID() {
        return this.roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public String getAnimationFrame() {
        return this.animationFrame;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setAnimationFrame(String animationFrame) {
        this.animationFrame = animationFrame;
    }

    public long getLastReceivedTime() {
        return lastReceivedTime;
    }

    public void updateLastReceivedTime() {
        this.lastReceivedTime = System.currentTimeMillis();
    }

    public List<Bullet> getPlayerObjects() {
        return this.playerObjects;
    }

    public void addPlayerObject(Bullet bullet) {
        this.playerObjects.add(bullet);
    }

    public void removePlayerObject(Bullet bullet) {
        this.playerObjects.remove(bullet);
    }

}