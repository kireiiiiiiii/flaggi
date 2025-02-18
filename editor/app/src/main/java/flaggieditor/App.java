/*
 * Author: Matěj Šťastný
 * Date created: 2/2/2025
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

package flaggieditor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

import flaggieditor.common.AdvancedVariable;
import flaggieditor.common.GPanel;
import flaggieditor.common.MapData;
import flaggieditor.common.GPanel.InteractableHandeler;
import flaggieditor.common.MapData.ObjectType;
import flaggieditor.widgets.MapRender;

/**
 * Main application class.
 * 
 */
public class App implements InteractableHandeler {

    /////////////////
    // Variables
    ////////////////

    private GPanel gpanel;

    /////////////////
    // MM & Constr
    ////////////////

    /**
     * Main method.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
        // validateMapDataSerialization();
        // validateMapDataDeserialization();
    }

    /**
     * Constructor.
     */
    public App() {

        // ----- Variable init
        int[] windowSize = getScreenSize();
        this.gpanel = new GPanel(this, 60, windowSize[0], windowSize[1], false, "Flaggi Editor", Color.BLACK);

        // ----- Widgets
        gpanel.add(new MapRender(getPlaceholderMap()));

    }

    /////////////////
    // Helpers
    ////////////////

    /**
     * Gets the screen size.
     * 
     * @return
     */
    private static int[] getScreenSize() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new int[] { (int) screenSize.getWidth(), (int) screenSize.getHeight() };
    }

    /////////////////
    // Interactions
    ////////////////

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    /////////////////
    // Tests
    ////////////////

    /**
     * Gets example map data.
     * 
     * @return a placeholder {@code MapData} object.
     */
    public static MapData getPlaceholderMap() {
        MapData map = new MapData("My Awsome Map", 5_000, 10_000);

        map.setSpawn(100, 200, 670, 240);
        map.newGameObject(ObjectType.TREE, 500, 1000);
        map.newGameObject(ObjectType.TREE, 720, 976);

        return map;
    }

    /**
     * Test the map data serialization proccess.
     * 
     */
    public static void validateMapDataSerialization() {
        try {

            String filePath = System.getenv("HOME") + File.separator + "flaggimap.json";

            AdvancedVariable<MapData> map = new AdvancedVariable<MapData>(filePath);
            map.set(getPlaceholderMap());
            map.save();
            map.printJsonData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test the map data serialization proccess.
     * 
     */
    public static void validateMapDataDeserialization() {
        try {

            String filePath = System.getenv("HOME") + File.separator + "flaggimap.json";
            AdvancedVariable<MapData> map = new AdvancedVariable<MapData>(filePath);
            map.loadFromFile(MapData.class);
            map.get().printData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
