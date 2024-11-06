/*
 * Author: Matěj Šťastný
 * Date created: 11/4/2024
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

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import kireiiiiiiii.GPanel.InteractableHandeler;
import kireiiiiiiii.GPanel.Renderable;
import kireiiiiiiii.constants.ZIndex;
import kireiiiiiiii.ui.Background;
import kireiiiiiiii.ui.Player;

/**
 * Main class for the LAN Game application.
 */
public class App implements InteractableHandeler {

    /////////////////
    // Constants
    ////////////////

    public static final int TCP_PORT = 54321;
    private static final int FPS = 60;

    /////////////////
    // Variables
    ////////////////

    private Client client;
    private final int[] pos = { 250, 250 }; // Initialize player position
    private GPanel gpanel;

    /////////////////
    // Main & Constructor
    ////////////////

    public static void main(String[] args) {
        Logger.addLog("App started", false);
        SwingUtilities.invokeLater(App::new);
    }

    public App() {

        if (!isServerRunning(TCP_PORT)) {
            System.out.println("Cannot reach server.");
            Logger.addLog("Coudn't reach the server - server is running. Exiting...", true);
            System.exit(1);
        }

        this.pos[0] = 250;
        this.pos[1] = 250;
        Scanner console = new Scanner(System.in);
        printHeader();

        System.out.print("\nEnter your name: ");
        String username = console.nextLine();
        Logger.addLog("User entered name: " + username, true);

        client = new Client(username);

        this.gpanel = new GPanel(this, FPS, 500, 500, false, "Java LAN game");
        initializeWidgets();
        Logger.addLog("UI window created", true);

        GameLoop gameLoop = new GameLoop(FPS);
        gameLoop.start();
        Logger.addLog("Game loop started", true);
        console.close();
    }

    /////////////////
    // Events
    ////////////////

    /**
     * Updates the local position of this player.
     * 
     * @param x - new X value.
     * @param y - new Y value.
     */
    public void updateLocalPosition(int x, int y) {
        pos[0] = x;
        pos[1] = y;
    }

    /**
     * Initializes all new widgets and adds them to the gpanel.
     * 
     */
    public void initializeWidgets() {
        gpanel.add(new Background());
    }

    /////////////////
    // Interactable
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
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                this.pos[1] -= 10;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                this.pos[1] += 10;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                this.pos[0] -= 10;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                this.pos[0] += 10;
                break;
            default:
                break;
        }
    }

    /**
     * Prints the header of this game
     * 
     */
    private void printHeader() {
        System.out.println("\n" +
                "     ____.  _________   _________    .____       _____    _______   \n" +
                "    |    | /  _  \\   \\ /   /  _  \\   |    |     /  _  \\   \\      \\  \n" +
                "    |    |/  /_\\  \\   Y   /  /_\\  \\  |    |    /  /_\\  \\  /   |   \\ \n" +
                "/\\__|    /    |    \\     /    |    \\ |    |___/    |    \\/    |    \\\n" +
                "\\________\\____|__  /\\___/\\____|__  / |_______ \\____|__  /\\____|__  /\n" +
                "                 \\/              \\/          \\/       \\/         \\/ \n");
    }

    /**
     * Finds out whether is something running on a specific port by trying to create
     * a new socket.
     * 
     * @param port - target port
     * @return boolean value
     */
    private static boolean isServerRunning(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    /**
     * Game loop for the application.
     * 
     */
    @SuppressWarnings("unused")
    private class GameLoop implements Runnable {
        private boolean running = false;
        private int targetFPS;

        public GameLoop(int fps) {
            setFps(fps);
        }

        public void start() {
            running = true;
            new Thread(this, "Game loop Thread").start();
        }

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                long optimalTime = 1_000_000_000 / targetFPS;
                long startTime = System.nanoTime();

                update(); // Update game state

                long elapsedTime = System.nanoTime() - startTime;
                long sleepTime = optimalTime - elapsedTime;

                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restore interrupted status
                    }
                }
            }
        }

        private void update() {
            ArrayList<int[]> positions = client.updatePlayerPositions(pos[0], pos[1]);
            ArrayList<Renderable> players = new ArrayList<Renderable>();
            gpanel.removeWidgetsOfClass(Player.class);
            players.add(new Player(pos, Color.BLUE, ZIndex.PLAYER));
            for (int[] position : positions) {
                players.add(new Player(position, Color.RED, ZIndex.OTHER_PLAYERS));
            }
            gpanel.add(players);
        }

        public void setFps(int value) {
            targetFPS = value;
        }
    }
}
