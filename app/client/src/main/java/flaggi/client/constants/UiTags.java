/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 7/25/2024
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggi.client.constants;

public class UiTags {

    // Private constructor to prevent instantiation
    private UiTags() {
        throw new UnsupportedOperationException("UiIndex is a constants class and cannot be instantiated.");
    }

    // Constants ----------------------------------------------------------------

    public static final String DEBUG = "debug";

    public static final String MENU_ELEMENTS = "menu";
    public static final String GAME_ELEMENTS = "game";
    public static final String ENEMY_PLAYER = "enemy";
    public static final String PAUSE_MENU = "pause";
    public static final String LOBBY = "lobby";

    public static final String ENVIRONMENT = "environment";
    public static final String PROJECTILES = "projectiles";

}
