/*
 * Author: Matěj Šťastný
 * Date created: 12/12/2024
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
 */

package flaggiserver.common;

/**
 * Rectangle class to replace the {@code java.awt.Rectangle} class.
 * 
 */
public class Rectangle {

    /////////////////
    // Variables
    ////////////////

    private int x, y, width, height;

    /**
     * Empty constructor. Initializes all values with 0.
     * 
     */
    public Rectangle() {
        this(0, 0, 0, 0);
    }

    /**
     * Basic constructor with width and height
     * 
     * @param width  - width of the rectangle.
     * @param height - height of the rectangle.
     */
    public Rectangle(int width, int height) {
        this(0, 0, width, height);
    }

    /**
     * Constructor initializing all fields of the rectangle.
     * 
     * @param x      - X position.
     * @param y      - Y position.
     * @param width  - width.
     * @param height - height.
     */
    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
    }

    /**
     * Copy constructor. Clones all the values of the param rectangle.
     * 
     * @param rect - target rectangle.
     */
    public Rectangle(Rectangle rect) {
        this(rect.x, rect.y, rect.width, rect.height);
    }

    /////////////////
    // Accesors & setters
    ////////////////

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = Math.max(0, width);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = Math.max(0, height);
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(int width, int height) {
        this.width = Math.max(0, width);
        this.height = Math.max(0, height);
    }

    /////////////////
    // Public methods
    ////////////////

    /**
     * Check if this rectangle contains a given point.
     * 
     * @param px - X coordinate of target point.
     * @param py - Y coordinate of target point.
     * @return true if the point is inside the rectangle, false otherwise.
     */
    public boolean contains(int px, int py) {
        return px >= x && px < x + width && py >= y && py < y + height;
    }

    /**
     * Check if this rectangle intersects with another rectangle.
     * 
     * @param rect - target rectangle
     * @return true if the rectangles intersect, false otherwise.
     */
    public boolean intersects(Rectangle rect) {
        return rect.x < this.x + this.width && rect.x + rect.width > this.x && rect.y < this.y + this.height && rect.y + rect.height > this.y;
    }

    /**
     * Gets the intersection of this rectangle with another rectangle.
     * 
     * @param rect - target rectangle
     * @return a new {@code Rectangle} object of the intersection area.
     */
    public Rectangle intersection(Rectangle rect) {
        int newX = Math.max(this.x, rect.x);
        int newY = Math.max(this.y, rect.y);
        int newWidth = Math.min(this.x + this.width, rect.x + rect.width) - newX;
        int newHeight = Math.min(this.y + this.height, rect.y + rect.height) - newY;

        if (newWidth <= 0 || newHeight <= 0) {
            return new Rectangle(); // No intersection
        }

        return new Rectangle(newX, newY, newWidth, newHeight);
    }

    /**
     * Check if this rectangle contains another rectangle.
     * 
     * @param rect - target rectangle.
     * @return true if the rectangle contains the other rectangle, false otherwise.
     */
    public boolean contains(Rectangle rect) {
        return rect.x >= this.x && rect.y >= this.y && rect.x + rect.width <= this.x + this.width && rect.y + rect.height <= this.y + this.height;
    }

    @Override
    public String toString() {
        return "Rectangle[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]";
    }

}
