/*
 * Author: Matěj Šťastný
 * Date created: 11/4/2024
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
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;

import flaggi.common.GPanel.Renderable;
import flaggi.common.Sprite;
import flaggi.constants.WidgetTags;
import flaggi.util.FontUtil;

/**
 * Player widget class.
 * 
 */
public class Player implements Renderable {

    /////////////////
    // Variables
    ////////////////

    private int[] pos = new int[2];
    private boolean visible = true;
    private boolean isEnemy;
    private int zindex, id;
    private String name;
    private Sprite sprite;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Default constructor.
     * 
     * @param pos     - position of the player.
     * @param zindex  - ZIndex of the player (different for local player, and enemy
     *                players).
     * @param name    - name of the player.
     * @param isEnemy - is the player an enemy?
     * @param id      - id of the player. Used to update the position of the
     *                players.
     */
    public Player(int[] pos, int zindex, String name, boolean isEnemy, int id) {
        this.sprite = new Sprite();
        this.pos = pos;
        this.zindex = zindex;
        this.name = name;
        this.isEnemy = isEnemy;
        this.id = id;

        // Set the sprite texture :3
        if (isEnemy) {
            this.sprite.addAnimation(Arrays.asList("red_player_idle"), "idle");
        } else {
            this.sprite.addAnimation(Arrays.asList("blue_player_idle"), "idle");
        }
        this.sprite.setAnimation("idle");
        this.sprite.setFps(2);
        this.sprite.play();
    }

    /////////////////
    // Render methods
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] offset, Container focusCycleRootAncestor) {
        if (!this.isEnemy) {
            offset = new int[] { 0, 0 };
        }

        // Render the player sprite
        this.sprite.render(g, this.pos[0] + offset[0], this.pos[1] + offset[1], focusCycleRootAncestor);

        // Render the nametag
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        int[] pos = FontUtil.getCenteredPos(55, 5, g.getFontMetrics(),
                this.name);
        pos[1] = 25;
        g.drawString(this.name, offset[0] + this.pos[0] + pos[0], offset[1] + this.pos[1] + pos[1] - 40);

    }

    @Override
    public int getZIndex() {
        return this.zindex;
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
        ArrayList<String> tags = new ArrayList<String>();
        tags.add(WidgetTags.GAME_ELEMENTS);
        if (isEnemy) {
            tags.add(WidgetTags.ENEMY_PLAYER);
        }
        return tags;
    }

    /////////////////
    // Modifiers & Accesors
    ////////////////

    /**
     * Set a new player position.
     * 
     * @param pos - value.
     */
    public void setPos(int[] pos) {
        this.pos = pos;
    }

    /**
     * Returns {@code true} if the player is an enemy. Otherwise, returns
     * {@code false}.
     * 
     * @return - {@code boolean}.
     */
    public boolean isEnemy() {
        return this.isEnemy;
    }

    /**
     * Returns the {@code ID} of this player, given by the server.
     * 
     * @return - {@code int} of the ID.
     */
    public int getId() {
        return this.id;
    }

}
