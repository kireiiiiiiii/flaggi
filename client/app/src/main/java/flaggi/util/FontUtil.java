/*
 * Author: Matěj Šťastný
 * Date created: 10/29/2024
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
 *
 */

package flaggi.util;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to controll font stuff.
 * 
 */
public class FontUtil {

    /////////////////
    // Font file handeling methods
    ////////////////

    /**
     * Loads a font from the resources directory in the JAR.
     *
     * @param resourcePath - the relative path to the font file inside the resources
     *                     - directory.
     * @return The loaded Font object.
     * @throws IOException         if there is an error reading the font file.
     * @throws FontFormatException if the font file format is invalid.
     */
    public static Font loadFont(String resourcePath) throws IOException, FontFormatException {
        // Load the font file as an InputStream from the resources directory
        try (InputStream fontStream = FontUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (fontStream == null) {
                throw new IOException("Font file not found: " + resourcePath);
            }

            // Create a Font object from the InputStream
            return Font.createFont(Font.TRUETYPE_FONT, fontStream);
        }
    }

    /////////////////
    // Text position methods
    ////////////////
    /**
     * Calculates the centered position of a text in a {@code JPanel}, and return a
     * position that can be taken as input to the {@code drawString()} method.
     * 
     * @param width  - width of the rectangle that the position is being calculated
     *               from. Usually width of the {@code JPanel}.
     * @param height - height of the rectangle that the position is being calculated
     *               from. Usually height of the {@code JPanel}.
     * @param fm     - {@code FontMetric} of the target font.
     * @param text   - target text.
     * @return an {@code int} array of lenght 2, where index 0 represents {@code x},
     *         and index 1 represents {@code y} positions.
     * 
     */
    public static int[] getCenteredPos(int width, int height, FontMetrics fm, String text) {
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        int textX = (width - textWidth) / 2;
        int textY = (height - textHeight) / 2 + fm.getAscent();

        int[] pos = { textX, textY };
        return pos;
    }

    /**
     * Calculates the origin point to render the text bottom-left of the given
     * point.
     * 
     * @param fm    - FontMetrics object to get text dimensions
     * @param text  - The text to be rendered
     * @param point - The point represented by an int array [x, y]
     * @return The calculated point for rendering the text south-east of the given
     *         point
     */
    public static int[] getBottomRightTextOrigin(FontMetrics fm, String text, int[] point) {
        if (point == null || point.length < 2) {
            throw new IllegalArgumentException("Point array must have at least two elements");
        }

        int x = point[0];
        int y = point[1];

        // Get the height of the text
        int textHeight = fm.getHeight();

        // Calculate the new point south-east of the original point
        int caclX = x;
        int calcY = y + textHeight;

        return new int[] { caclX, calcY };
    }

    /**
     * Calculates the point to render the text bottom-left of the given point.
     * 
     * @param fm    - FontMetrics object to get text dimensions
     * @param text  - The text to be rendered
     * @param point - The point represented by an int array [x, y]
     * @return The calculated point for rendering the text south-west of the given
     *         point
     */
    public static int[] getBottomLeftOrigin(FontMetrics fm, String text, int[] point) {
        if (point == null || point.length < 2) {
            throw new IllegalArgumentException("Point array must have at least two elements");
        }

        int x = point[0];
        int y = point[1];

        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        int calcX = x - textWidth;
        int calcY = y + textHeight;

        int[] pos = { calcX, calcY };
        return pos;
    }

}
