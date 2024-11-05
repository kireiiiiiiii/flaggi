package kireiiiiiiii;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.util.ArrayList;

import kireiiiiiiii.GPanel.Renderable;

public class Player implements Renderable {

    private int[] pos = new int[2];
    private boolean visible = true;
    private Color color;

    public Player(int[] pos, Color color) {
        this.pos = pos;
        this.color = color;
    }

    @Override
    public void render(Graphics2D g, int[] size, Container focusCycleRootAncestor) {
        g.setColor(this.color);
        g.fillRect(this.pos[0], this.pos[1], 50, 50);
    }

    @Override
    public int getZIndex() {
        return 10;
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
