/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 11/4/2024 (v2 - 2/25/2025)
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggi.client.constants;

public class Constants {

    // Private constructor to prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("Constants is a constants class and cannot be instantiated.");
    }

    // Window -------------------------------------------------------------------

    public static String WINDOW_NAME = "Flaggi";
    public static int[] BASE_WINDOW_SIZE = { 1200, 600 };
    public static int FRAMERATE = 120;

    // Debug --------------------------------------------------------------------

    public static boolean HITBOXES_ENABLED = false;

    // Game ---------------------------------------------------------------------

    public static final int LOBBY_UPDATE_FETCH_INTERVAL_SEC = 3;

}
