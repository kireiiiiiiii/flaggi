/*
 * Author: Matěj Šťastný
 * Date created: 10/29/2024
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

package kireiiiiiiii;

import java.awt.FontMetrics;

public class FontUtil {
    /////////////////
    // Measurment calculating methods
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
     *         and index 1
     *         represents {@code y} positions.
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
