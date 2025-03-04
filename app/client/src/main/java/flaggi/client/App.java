/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 11/4/2024 (v2 - 2/25/2025)
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggi.client;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import flaggi.client.constants.Constants;
import flaggi.client.ui.MenuScreen;
import flaggi.client.ui.ScreenTest;
import flaggi.shared.common.GPanel;
import flaggi.shared.common.GPanel.InteractableHandler;
import flaggi.shared.util.ScreenUtil;

public class App implements InteractableHandler {

    private final GPanel gpanel;

    // Main ---------------------------------------------------------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(App::new);
    }

    public App() {
        int[] screenSize = ScreenUtil.getScreenDimensions();
        this.gpanel = getDefaultGpanel(screenSize[0], screenSize[1]);
        this.gpanel.setFpsCap(Constants.FRAMERATE);
        addDefaultWidgets();
    }

    // Private ------------------------------------------------------------------

    private GPanel getDefaultGpanel(int width, int height) {
        double scaleX = (double) width / Constants.BASE_WINDOW_SIZE[0];
        double scaleY = (double) height / Constants.BASE_WINDOW_SIZE[1];
        double scale = Math.min(scaleX, scaleY) * 0.9;
        return new GPanel(Constants.BASE_WINDOW_SIZE[0], Constants.BASE_WINDOW_SIZE[1], false, Constants.WINDOW_NAME, new double[] { scale, scale }, this);
    }

    private void addDefaultWidgets() {
        this.gpanel.add(new MenuScreen(null, null, null));
    }

    // Interaction --------------------------------------------------------------

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
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
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
}
