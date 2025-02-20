package flaggiclient.ui;

import java.awt.Container;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;

import flaggiclient.common.Sprite;
import flaggiclient.constants.WidgetTags;
import flaggiclient.constants.ZIndex;
import flaggishared.GPanel.Renderable;

public class Flag implements Renderable {

    Sprite sprite;
    boolean visible;
    int[] position;

    public Flag(int[] position, boolean isBlue) {
        this.position = position;
        this.sprite = new Sprite();
        this.sprite.addAnimation(Arrays.asList("flag-blue"), "flag-blue");
        this.sprite.addAnimation(Arrays.asList("flag-red"), "flag-red");
        this.sprite.setAnimation(isBlue ? "flag-blue" : "flag-red");
        this.visible = true;
    }

    @Override
    public void render(Graphics2D g, int[] size, int[] origin, Container focusCycleRootAncestor) {
        this.sprite.render(g, this.position[0] + origin[0], this.position[1] + origin[1], focusCycleRootAncestor);
    }

    @Override
    public int getZIndex() {
        return ZIndex.ENVIRONMENT_BOTTOM;
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
        return tags;
    }

}
