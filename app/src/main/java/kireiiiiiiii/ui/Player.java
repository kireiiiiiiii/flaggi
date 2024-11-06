/*
 * Author: Matěj Šťastný
 * Date created: 11/4/2024
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

package kireiiiiiiii.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;

import kireiiiiiiii.FontUtil;
import kireiiiiiiii.GPanel.Renderable;

public class Player implements Renderable {

    private int[] pos = new int[2];
    private boolean visible = true;
    private Color color;
    private int zindex;
    private String name;

    public Player(int[] pos, Color color, int zindex, String name) {
        this.pos = pos;
        this.color = color;
        this.zindex = zindex;
        this.name = name;
    }

    @Override
    public void render(Graphics2D g, int[] size, Container focusCycleRootAncestor) {
        g.setColor(this.color);
        g.fillRect(this.pos[0], this.pos[1], 50, 50);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        int[] pos = FontUtil.getCenteredPos(50, 50, g.getFontMetrics(), this.name);
        g.drawString(this.name, this.pos[0] + pos[0], this.pos[1] + pos[1] - 40);

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
        return null;
    }

    public void setPos(int[] pos) {
        this.pos = pos;
    }

}