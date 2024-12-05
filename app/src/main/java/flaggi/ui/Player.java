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
import flaggi.common.Sprite;
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;
import flaggi.util.FileUtil;
import flaggi.util.FontUtil;

/**
 * Player widget class.
 * 
 */
public class Player implements Renderable {

    /////////////////
    // Constants
    ////////////////

    public static final String DEFAULT_SKIN = "default_blue";
    public static final String DEFAULT_ENEMY_SKIN = "default_red";

    /////////////////
    // Animation libraries
    ////////////////

    private static Map<String, List<Image>> playerAnimationsLibrary;

    /////////////////
    // Variables
    ////////////////

    private int[] pos = new int[2];
    private boolean visible = true;
    private int id;
    private String name, animationFrame, localPlayerSkinName;
    private Sprite sprite;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Default constructor.
     * 
     * @param pos      - position of the player.
     * @param name     - name of the player.
     * @param skinName - the name of the skin the player is using.
     * @param isEnemy  - is the player an enemy?
     * @param id       - id of the player. Used to update the position of the
     *                 players.
     */
    public Player(int[] pos, String name, String skinName, int id) {
        // ---- Use the default constructor, but with a null value
        this(pos, name, id, null);

        // ---- Set animation
        this.localPlayerSkinName = skinName;
        this.sprite.setAnimation(localPlayerSkinName + "_idle");
        this.sprite.setFps(2);
        this.sprite.play();
    }

    /**
     * Constructor for players with a specific animationFrame
     * 
     * @param pos     - position of the player.
     * @param name    - name of the player.
     * @param isEnemy - is the player an enemy?
     * @param id      - id of the player. Used to update the position of the
     *                players.
     */
    public Player(int[] pos, String name, int id, String animationFrame) {

        // ---- Set variables
        this.sprite = new Sprite();
        this.animationFrame = animationFrame;
        this.pos = pos;
        this.name = name;
        this.id = id;

        // ---- Initialize skins
        if (playerAnimationsLibrary == null) {
            addAllPlayerAnimations();
        }
        this.sprite.setAnimations(playerAnimationsLibrary);
    }

    /////////////////
    // Static methods
    ////////////////

    /**
     * Add all the downloaded sprite animations.
     * 
     */
    private static void addAllPlayerAnimations() {
        playerAnimationsLibrary = new HashMap<>();
        String[] skinTextures = FileUtil.listDirectoriesInJar("sprites/player");

        // Add idle animations
        for (String skinName : skinTextures) {
            List<String> frameNames;

            frameNames = getAnimationList(skinName, Arrays.asList("idle_1", "idle_2"));
            playerAnimationsLibrary.put(skinName + "_idle", Sprite.loadFrames(frameNames));

            frameNames = getAnimationList(skinName, Arrays.asList("walk_side", "walk_side_l", "walk_side", "walk_side_r"));
            playerAnimationsLibrary.put(skinName + "_walk_side", Sprite.loadFrames(frameNames));

        }
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
            String[] parsedFrameData = parseAnimationFrame(this.animationFrame);
            if (parsedFrameData.length != 2) {
                return;
            }
            String animationName = parsedFrameData[0];
            int animationFrame = Integer.parseInt(parsedFrameData[1]);
            this.sprite.render(g, this.pos[0] + offset[0], this.pos[1] + offset[1], focusCycleRootAncestor, animationName, animationFrame);
        }

        // Render the nametag
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        int[] pos = FontUtil.getCenteredPos(55, 5, g.getFontMetrics(), this.name);
        pos[1] = 25;
        g.drawString(this.name, offset[0] + this.pos[0] + pos[0], offset[1] + this.pos[1] + pos[1] - 40);

    }

    @Override
    public int getZIndex() {
        return this.isEnemy() ? ZIndex.OTHER_PLAYERS : ZIndex.PLAYER;
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
     * value, and frame value. If the skin data is the default skin, it will be
     * changed to use the default enemy skin.
     * 
     * @param animationFrame - target message.
     * @return - {@code String} array, index 0 with animation name, index 1 with
     *         animation frame.
     */
    private static String[] parseAnimationFrame(String animationFrame) {
        String[] data = animationFrame.split(":");
        if (data.length != 2) {
            return new String[] { DEFAULT_ENEMY_SKIN + "_idle", "0" };
        }
        if (data[0].startsWith(DEFAULT_SKIN)) {
            data[0] = DEFAULT_ENEMY_SKIN + data[0].substring(DEFAULT_SKIN.length());
        }
        return data;
    }

    /**
     * Formats a list of animation names to be player sprite animations.
     * 
     * @param animationName - name of the animation.
     * @param skinName      - name of the skin the animation is for.
     * @param frameNames    - frame names.
     * @return List that can be passed into the sprite constructor.
     */
    private static List<String> getAnimationList(String skinName, List<String> frameNames) {
        List<String> framePaths = new ArrayList<String>();
        for (String frameName : frameNames) {
            String path = "player/" + skinName + "/" + frameName;
            framePaths.add(path);
        }
        return framePaths;
    }

}
