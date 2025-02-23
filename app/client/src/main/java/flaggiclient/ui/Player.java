/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 11/4/2024
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggiclient.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import flaggiclient.App;
import flaggiclient.common.Sprite;
import flaggiclient.constants.WidgetTags;
import flaggiclient.constants.ZIndex;
import flaggishared.common.GPanel.Renderable;
import flaggishared.util.FileUtil;
import flaggishared.util.FontUtil;

/**
 * Player widget class.
 */
public class Player extends Renderable {

    public static final int TEXTURE_HEIGHT = 20 * Sprite.SPRITE_SCALING;
    public static final int TEXTURE_WIDTH = 13 * Sprite.SPRITE_SCALING;
    public static final String DEFAULT_ENEMY_SKIN = "default_red";
    public static final String DEFAULT_SKIN = "default_blue";
    private static Map<String, List<Image>> playerAnimationsLibrary;

    private String name, animationFrame, localPlayerSkinName;
    private boolean inverted = false, hasFlag = false;
    private int[] position = new int[2];
    private Sprite avatar, flag;
    private int id, health;

    // Constructors -------------------------------------------------------------

    public Player(int[] position, String name, String skinName, int id) {
        this(position, name, id, null);

        this.localPlayerSkinName = skinName;
        this.avatar.setAnimation(localPlayerSkinName + "_idle");
        this.avatar.setFps(2);
        this.avatar.play();

        this.flag.setAnimation("flag_blue");
        this.flag.setFps(2);
        this.flag.play();
    }

    /**
     * Enemy players
     */
    public Player(int[] pos, String name, int id, String animationFrame) {
        super(ZIndex.PLAYER, WidgetTags.GAME_ELEMENTS);
        if (animationFrame == null) {
            this.setZIndex(ZIndex.OTHER_PLAYERS);
            this.addTag(WidgetTags.ENEMY_PLAYER);
        }

        this.avatar = new Sprite();
        this.flag = new Sprite();
        this.flag.addAnimation(Arrays.asList("flag-blue"), "flag_blue");
        this.flag.addAnimation(Arrays.asList("flag-red"), "flag_red");
        this.flag.setAnimation("flag_red");
        this.animationFrame = animationFrame;
        this.position = pos;
        this.name = name;
        this.id = id;

        if (playerAnimationsLibrary == null) {
            addAllAvatarAnimations();
        }
        this.avatar.setAnimations(playerAnimationsLibrary);
    }

    // Rendering ----------------------------------------------------------------

    @Override
    public void render(Graphics2D g, int[] size, int[] offset, Container focusCycleRootAncestor) {

        if (this.hasFlag) {
            this.flag.render(g, this.position[0], this.position[1], focusCycleRootAncestor, this.inverted);
        }

        if (!isEnemy()) {
            this.avatar.render(g, this.position[0], this.position[1], focusCycleRootAncestor, this.inverted);
            offset = new int[] { 0, 0 };

        } else {
            String[] parsedFrameData = parseAnimationFrame(this.animationFrame);
            String animationName = parsedFrameData[0];
            int animationFrame = Integer.parseInt(parsedFrameData[1]);
            boolean inverted = Boolean.parseBoolean(parsedFrameData[2]);
            this.avatar.render(g, this.position[0] + offset[0], this.position[1] + offset[1], focusCycleRootAncestor, animationName, animationFrame, inverted);
        }

        // Render the nametag
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        String nameTagText = this.name;
        int[] namePos = FontUtil.calculateCenteredPosition(55, 5, g.getFontMetrics(), nameTagText);
        namePos[1] = -30;
        g.drawString(nameTagText, offset[0] + this.position[0] + namePos[0], offset[1] + this.position[1] + namePos[1]);

        // Render the health bar
        int barWidth = 50;
        int barHeight = 5;
        int x = offset[0] + this.position[0] - barWidth / 2 + 27;
        int y = offset[1] + this.position[1] - 20;

        // Background of the health bar (gray)
        g.setColor(Color.GRAY);
        g.fillRect(x, y, barWidth, barHeight);

        // Foreground of the health bar (red for health)
        if (this.isEnemy()) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.BLUE);
        }
        int healthWidth = (int) ((this.health / 100.0) * barWidth);
        g.fillRect(x, y, healthWidth, barHeight);

        // Border for the health bar
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.drawRect(x, y, barWidth, barHeight);

        // ---- Hitboxes if turned on
        if (App.SHOW_HITBOXES) {
            g.setStroke(new BasicStroke(1));
            g.setColor(Color.RED);
            Rectangle r;
            if (!isEnemy()) {
                r = new Rectangle(this.position[0] + 7, this.position[1] + 7, 53, 93);
            } else {
                r = new Rectangle(this.position[0] + 7 + offset[0], this.position[1] + 7 + offset[1], 53, 93);
            }
            g.draw(r);
        }

    }

    // Modifiers ----------------------------------------------------------------

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

    public void setPosition(int[] position) {
        this.position = position;
    }

    public void setFacingRight(boolean invert) {
        this.inverted = invert;
    }

    public void setHasFlag(boolean hasFlag) {
        this.hasFlag = hasFlag;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    // Accesors -----------------------------------------------------------------

    /**
     * Makes the current animation frame to send to the server.
     *
     * @return - {@code String} in the form of
     *         "current-animation-name:current-frame".
     */
    public String getAnimationFrame() {
        return this.avatar.getAnimationFrame() + ":" + this.inverted;
    }

    public boolean isEnemy() {
        return this.animationFrame != null;
    }

    public int getId() {
        return this.id;
    }

    public void hasFlag(boolean hasFlag) {
        this.hasFlag = hasFlag;
    }

    /**
     * Switches the player animation.
     *
     * @param name - name of the player animation. (skin name not needed)
     */
    public void switchAnimation(String name) {
        String animationName = this.localPlayerSkinName + "_" + name;
        if (this.avatar.getAnimationFrame().split(":")[0].equals(animationName)) {
            return;
        }
        this.avatar.setAnimation(animationName);
        if (name.equals("idle")) {
            this.avatar.setFps(2);
        } else {
            this.avatar.setFps(4);
        }
    }

    // Private ------------------------------------------------------------------

    /**
     * Player skins initialization.
     */
    private static void addAllAvatarAnimations() {
        playerAnimationsLibrary = new HashMap<>();
        String[] skinTextures = FileUtil.retrieveJarDirectoryList("sprites/player");

        for (String skinName : skinTextures) {
            List<String> frameNames;

            try {
                frameNames = getAnimationList(skinName, Arrays.asList("idle_1", "idle_2"));
                playerAnimationsLibrary.put(skinName + "_idle", Sprite.loadFrames(frameNames));

                frameNames = getAnimationList(skinName, Arrays.asList("walk_side", "walk_side_l", "walk_side", "walk_side_r"));
                playerAnimationsLibrary.put(skinName + "_walk_side", Sprite.loadFrames(frameNames));

                frameNames = getAnimationList(skinName, Arrays.asList("walk_diagup", "walk_diagup_l", "walk_diagup", "walk_diagup_r"));
                playerAnimationsLibrary.put(skinName + "_walk_diagup", Sprite.loadFrames(frameNames));

                frameNames = getAnimationList(skinName, Arrays.asList("walk_up", "walk_up_l", "walk_up", "walk_up_r"));
                playerAnimationsLibrary.put(skinName + "_walk_up", Sprite.loadFrames(frameNames));

                frameNames = getAnimationList(skinName, Arrays.asList("walk_down", "walk_down_l", "walk_down", "walk_down_r"));
                playerAnimationsLibrary.put(skinName + "_walk_down", Sprite.loadFrames(frameNames));
            } catch (Exception e) {
                App.LOGGER.addLog("Error loading player animations for skin " + skinName + ".", e);
            }

        }
    }

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
        if (data.length != 3) {
            return new String[] { DEFAULT_ENEMY_SKIN + "_idle", "0", "false" };
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
