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
        this.pos = pos;
        this.zindex = zindex;
        this.name = name;
        this.isEnemy = isEnemy;
        this.id = id;

        // Set the sprite texture :3
        if (isEnemy) {
            this.sprite = new Sprite("red_player_idle");
        } else {
            this.sprite = new Sprite("blue_player_idle");
        }
        this.sprite.scaleToWidth(50);
    }

    /////////////////
    // Render methods
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] offset, Container focusCycleRootAncestor) {
        if (!this.isEnemy) {
            offset = new int[] { 0, 0 };
        }

        // g.setColor(this.isEnemy ? Color.RED : Color.BLUE);
        // g.fillRect(this.pos[0] + offset[0], this.pos[1] + offset[1], 50, 50);
        this.sprite.paint(g, this.pos[0] + offset[0], this.pos[1] + offset[1], focusCycleRootAncestor);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        int[] pos = FontUtil.getCenteredPos(50, 50, g.getFontMetrics(), this.name);
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

    public boolean isEnemy() {
        return this.isEnemy;
    }

    public int getId() {
        return this.id;
    }

}
