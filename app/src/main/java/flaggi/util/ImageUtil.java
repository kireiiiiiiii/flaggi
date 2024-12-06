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
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import flaggi.App;

/**
 * A utillity method class designed to handle images
 * 
 */
public class ImageUtil {

    /////////////////
    // Image from file getter methods
    ////////////////

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
     * @param image            - target {@code Image} object.
     * @param width            - target width of the image.
     * @param height           - target height of the image.
     * @param useSmoothScaling - {@code true} to enable smooth scaling;
     *                         {@code false} otherwise.
     * @return a new {@code Image} object reference of the scaled image.
     */
    public static Image scaleImage(Image image, int width, int height, boolean useSmoothScaling) {
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics2D = scaledImage.createGraphics();

        if (useSmoothScaling) {
            graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        graphics2D.drawImage(image, 0, 0, width, height, null);
        graphics2D.dispose();

        return scaledImage;
    }

    /**
     * Scales an {@code Image} object to a desired width, while maintaining the
     * original aspect ratio of the image.
     * 
     * @param image            - target {@code Image} object.
     * @param width            - target width.
     * @param useSmoothScaling - {@code true} to enable smooth scaling;
     *                         {@code false} otherwise.
     * @return a new {@code Image} object reference of the scaled image.
     */
    public static Image scaleToWidth(Image image, int width, boolean useSmoothScaling) {
        int originalWidth = image.getWidth(null);
        int originalHeight = image.getHeight(null);

        // Calculate the new height to maintain aspect ratio
        int height = (int) ((double) originalHeight * width / originalWidth);

        return scaleImage(image, width, height, useSmoothScaling);
    }

    /**
     * Scales an {@code Image} object to a desired height, while maintaining the
     * original aspect ratio of the image.
     * 
     * @param image            - target {@code Image} object.
     * @param height           - target height.
     * @param useSmoothScaling - {@code true} to enable smooth scaling;
     *                         {@code false} otherwise.
     * @return a new {@code Image} object reference of the scaled image.
     */
    public static Image scaleToHeight(Image image, int height, boolean useSmoothScaling) {
        int originalWidth = image.getWidth(null);
        int originalHeight = image.getHeight(null);

        // Calculate the new width to maintain aspect ratio
        int width = (int) ((double) originalWidth * height / originalHeight);

        return scaleImage(image, width, height, useSmoothScaling);
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
     * Vertically inverts an Image (flips it along the vertical axis).
     *
     * @param originalImage The original Image to be flipped.
     * @return A new Image that is vertically flipped.
     */
    public static Image flipImageVertically(Image originalImage) {
        if (originalImage == null) {
            throw new IllegalArgumentException("The provided image cannot be null.");
        }

        // Convert to BufferedImage for manipulation
        BufferedImage bufferedImage = toBufferedImage(originalImage);

        // Create a new BufferedImage with the same dimensions
        BufferedImage flippedImage = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), bufferedImage.getType());

        Graphics2D g2d = flippedImage.createGraphics();

        // Perform the vertical flip
        g2d.drawImage(bufferedImage, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), // destination rectangle
                bufferedImage.getWidth(), 0, 0, bufferedImage.getHeight(), // source rectangle (mirrored horizontally)
                null);
        g2d.dispose();

        return flippedImage;
    }

    /////////////////
    // Image scaling methods
    ////////////////

    /**
     * Gets the input stream of an image.
     * 
     * @param filePath - file name of the target image relative to the resources
     *                 folder.
     * @return a new {@code Image} object.
     */
    private static InputStream getImageInputStream(String filePath) {
        filePath = App.FILE_JAR_SEPARATOR + filePath;
        InputStream resourceStream = ImageUtil.class.getResourceAsStream(filePath);
        String exists = resourceStream != null ? "File exists." : "File does not exist.";
        App.LOGGER.addLog("Accesed resource input stream at path: '" + filePath + "'. " + exists);
        return resourceStream;
    }

    /**
     * Converts an Image to a BufferedImage.
     *
     * @param img - The Image to convert.
     * @return A BufferedImage representation of the input Image.
     */
    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a BufferedImage with the same width and height
        BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the Image onto the BufferedImage
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return bufferedImage;
    }

}
