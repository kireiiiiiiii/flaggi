/*
 * Author: Matěj Šťastný
 * Date created: 6/13/2024
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
 */

package flaggi.util;

import java.awt.Dimension;
import java.awt.Toolkit;

/**
 * Class to get information from the user screen.
 * 
 */
public class ScreenUtil {

    /**
     * Gets the screen dimensions of the user's screen using {@code Toolkit}.
     * 
     * @return new {@code Position} object of the dimensions.
     */
    public static int[] getScreenDimensions() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int[] dimensions = { screenWidth, screenHeight };
        return dimensions;
    }

}
