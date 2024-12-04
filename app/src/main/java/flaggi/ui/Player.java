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
import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flaggi.common.GPanel.Renderable;
import flaggi.common.Logger;
import flaggi.common.Sprite;
import flaggi.constants.WidgetTags;
import flaggi.util.FileUtil;
import flaggi.util.FontUtil;
import flaggi.util.ImageUtil;

/**
 * Player widget class.
 * 
 */
public class Player implements Renderable {

    public static final String CURRENT_SKIN = "default_blue";
    private static Map<String, List<Image>> animations;

    /////////////////
    // Variables
    ////////////////

    private int[] pos = new int[2];
    private boolean visible = true;
    private int zindex, id;
    private String name, animationFrame;
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
    public Player(int[] pos, int zindex, String name, int id) {
        // ---- Use the default constructor, but with a null value
        this(pos, zindex, name, id, null);

        // ---- Set animation
        this.sprite.setAnimation(CURRENT_SKIN + "_idle");
        this.sprite.setFps(2);
        this.sprite.play();
    }

    /**
     * Constructor for players with a specific animationFrame
     * 
     * @param pos     - position of the player.
     * @param zindex  - ZIndex of the player (different for local player, and enemy
     *                players).
     * @param name    - name of the player.
     * @param isEnemy - is the player an enemy?
     * @param id      - id of the player. Used to update the position of the
     *                players.
     */
    public Player(int[] pos, int zindex, String name, int id, String animationFrame) {

        // ---- Set variables
        this.sprite = new Sprite();
        this.animationFrame = animationFrame;
        this.pos = pos;
        this.zindex = zindex;
        this.name = name;
        this.id = id;

        // ---- Initialize skins
        if (animations == null) {
            addAllPlayerAnimations();
        }
        this.sprite.setAnimations(animations);
    }

    /////////////////
    // Render methods
    ////////////////

    @Override
    public void render(Graphics2D g, int[] size, int[] offset, Container focusCycleRootAncestor) {

        // Render the player sprite
        if (this.animationFrame == null) {
            this.sprite.render(g, this.pos[0], this.pos[1], focusCycleRootAncestor);
            offset = new int[] { 0, 0 };

        } else {
            // Enemy sprites
            String[] parsedFrameData = parseAnimationFrame(animationFrame);
            if (parsedFrameData.length != 2) {
                return;
            }
            String animationName = parsedFrameData[0];
            int animationFrame = Integer.parseInt(parsedFrameData[1]);
            this.sprite.render(g, this.pos[0] + offset[0], this.pos[1] + offset[1], focusCycleRootAncestor,
                    animationName, animationFrame);
        }

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
        if (this.animationFrame != null) {
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
        return this.animationFrame != null;
    }

    /**
     * Returns the {@code ID} of this player, given by the server.
     * 
     * @return - {@code int} of the ID.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Makes the current animation frame to send to the server.
     * 
     * @return - {@code String} in the form of
     *         "current-animation-name:current-frame".
     */
    public String getAnimationFrame() {
        return this.sprite.getAnimationFrame();
    }

    /**
     * Sets the animation frame value. This is used to render an exact animation
     * state of enemy players.
     * 
     * @param animationFrame - {@code String} in the form of
     *                       "current-animation-name:current-frame".
     */
    public void setAnimationFrameData(String animationFrame) {
        this.animationFrame = animationFrame;
    }

    /////////////////
    // Helper methods
    ////////////////

    /**
     * Parses the animation frame format {@code String} into an animation name
     * value, and frame value.
     * 
     * @param animationFrame - target message.
     * @return - {@code String} array, index 0 with animation name, index 1 with
     *         animation frame.
     */
    public static String[] parseAnimationFrame(String animationFrame) {
        return animationFrame.split(":");
    }

    /**
     * Add all the downloaded sprite animations.
     * 
     */
    public static void addAllPlayerAnimations() {
        animations = new HashMap<String, List<Image>>();
        String[] skinTextures = FileUtil.listDirectoriesInJar("sprites/player");
        for (String skinName : skinTextures) {
            List<String> frameNames = Arrays.asList(
                    "player/" + skinName + "/idle",
                    "player/" + skinName + "/l_leg_up",
                    "player/" + skinName + "/idle",
                    "player/" + skinName + "/r_leg_up");

            List<Image> frames = new ArrayList<>();
            for (String framePath : frameNames) {
                framePath = Sprite.SPRITE_RESOURCE_DIR_PATH + framePath + ".png";
                Image image = ImageUtil.getImageFromFile(framePath);
                if (image != null) {
                    image = ImageUtil.scaleImage(image,
                            image.getWidth(null) * Sprite.SPRITE_SCALING,
                            image.getHeight(null) * Sprite.SPRITE_SCALING,
                            false);
                    frames.add(image);
                } else {
                    Logger.addLog("Failed to load texture: '" + framePath + "'");
                }
            }
            animations.put(skinName + "_idle", frames);
            System.out.println("Added: '" + skinName + "_idle' skin with " + frames.size() + " frames.");
        }
        Logger.addLog("Loaded all player animations");
        System.out.println("NYA");
    }

}
