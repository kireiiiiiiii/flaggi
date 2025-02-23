/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 2/22/2025
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggi.server.constants;

import java.io.File;

import flaggishared.common.ConfigManager;
import flaggishared.util.FileUtil;

/**
 * A constants class storing all constants for this project.
 */
public class Constants {

    // Private constructor to prevent instantiation
    private Constants() {
        throw new UnsupportedOperationException("Constants is a constants class and cannot be instantiated.");
    }

    // Main constants -----------------------------------------------------------

    public static final String APP_DATA_DIR_NAME = "kireiiiiiiii.flaggi.server";
    private static final ConfigManager CONFIG_FILE = new ConfigManager(FileUtil.getApplicationDataFolder() + File.separator + Constants.APP_DATA_DIR_NAME + File.separator + "configs" + File.separator + "config.properties", "/configs/config.properties");;

    // Network ------------------------------------------------------------------

    public static final int TCP_PORT = CONFIG_FILE.getIntValue("tcp.port");
    public static final int UDP_PORT = CONFIG_FILE.getIntValue("udp.port");

}
