/*
 * Author: Matěj Šťastný
 * Date created: 5/16/2024
 * Github link: https://github.com/kireiiiiiiii/ShootingStars
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

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * A utillity method class designed to handle images
 * 
 */
public class ImageUtil {

    /////////////////
    // Image from file getter methods
    ////////////////

    /**
     * Reads an image from a file and than creates and return a new {@code Image}
     * object reference.
     * 
     * @param path - path/name of the target image file
     * @return a new {@code Image} from target file.
     * @throws IOException
     */
    public static Image loadImageFromFile(String path) throws IOException {
        File imageFile = new File(path);
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        return new ImageIcon(bufferedImage).getImage();
    }

    /**
     * Reads an image from a file and than creates and return a new {@code Image}
     * object reference.
     * 
     * @param imageFile - target image {@code File} object.
     * @return a new {@code Image} from target file.
     * @throws IOException
     */
    public static Image loadImageFromFile(File imageFile) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        return new ImageIcon(bufferedImage).getImage();
    }

    /////////////////
    // Image scaling methods
    ////////////////

    /**
     * Scales an {@code Image} object to the desired width and height.
     * 
     * @param image  - target {@code Image} object.
     * @param width  - target width of the image.
     * @param height - target height of the omage.
     * @return a new {@code Image} object reference of the scaled image.
     */
    public static Image scaleImage(Image image, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = scaledImage.createGraphics();

        // Set rendering hints to improve image quality
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();

        return scaledImage;
    }

    /**
     * Scales an {@code Image} object to a desired width, while leaving the original
     * height of the image.
     * 
     * @param image - target {@code Image} object.
     * @param width - target width.
     * @return a new {@code Image} object reference of the scaled image.
     */
    public static Image scaleToWidth(Image image, int width) {
        int height = image.getHeight(null);

        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = scaledImage.createGraphics();

        // Set rendering hints to improve image quality
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();

        return scaledImage;
    }

    /**
     * Scales an {@code Image} object to a desired height, while leaving the
     * original width of the image.
     * 
     * @param image  - target {@code Image} object.
     * @param height - target height.
     * @return a new {@code Image} object reference of the scaled image.
     */
    public static Image scaleToHeight(Image image, int height) {
        int width = image.getWidth(null);

        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = scaledImage.createGraphics();

        // Set rendering hints to improve image quality
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();

        return scaledImage;
    }

}
