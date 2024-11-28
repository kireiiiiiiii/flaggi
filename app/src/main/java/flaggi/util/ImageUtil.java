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
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * A utillity method class designed to handle images
 * 
 */
public class ImageUtil {

    /////////////////
    // Image from file getter methods
    ////////////////

    /**
     * Gets the input stream of an image.
     * 
     * @param filePath - file name of the target image relative to the resources
     *                 folder.
     * @return a new {@code Image} object.
     */
    private static InputStream getImageInputStream(String filePath) {
        return ImageUtil.class.getResourceAsStream(File.separator + filePath);
    }

    /**
     * Loads an image from the resources folder and returns it as an Image object.
     * 
     * @param imageName - file name of the image relative to the resources folder.
     * @return the loaded Image object or {@code null} if {@code IOExeption} occurs,
     *         or the {@code InputStream} is {@code null}
     */
    public static Image getImageFromFile(String imageName) {
        InputStream imageStream = getImageInputStream(imageName);
        if (imageStream == null) {
            return null;
        }
        BufferedImage img;
        try {
            img = ImageIO.read(imageStream);
        } catch (IOException e) {
            return null;
        }
        return img;
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
    public static Image stretchToWidth(Image image, int width) {
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
    public static Image stretchToHeight(Image image, int height) {
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

    /**
     * Scales an {@code Image} object to a desired width, while maintaining the
     * original
     * aspect ratio of the image.
     * 
     * @param image - target {@code Image} object.
     * @param width - target width.
     * @return a new {@code Image} object reference of the scaled image.
     */
    public static Image scaleToWidth(Image image, int width) {
        int originalWidth = image.getWidth(null);
        int originalHeight = image.getHeight(null);

        // Calculate the new height to maintain aspect ratio
        int height = (int) ((double) originalHeight * width / originalWidth);

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
     * Scales an {@code Image} object to a desired height, while maintaining the
     * original
     * aspect ratio of the image.
     * 
     * @param image  - target {@code Image} object.
     * @param height - target height.
     * @return a new {@code Image} object reference of the scaled image.
     */
    public static Image scaleToHeight(Image image, int height) {
        int originalWidth = image.getWidth(null);
        int originalHeight = image.getHeight(null);

        // Calculate the new width to maintain aspect ratio
        int width = (int) ((double) originalWidth * height / originalHeight);

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
