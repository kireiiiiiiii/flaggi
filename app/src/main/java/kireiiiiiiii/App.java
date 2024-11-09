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
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import kireiiiiiiii.common.Client;
import kireiiiiiiii.common.GPanel;
import kireiiiiiiii.common.Logger;
import kireiiiiiiii.common.Client.ClientStruct;
import kireiiiiiiii.common.GPanel.InteractableHandeler;
import kireiiiiiiii.common.GPanel.Renderable;
import kireiiiiiiii.constants.WidgetTags;
import kireiiiiiiii.constants.ZIndex;
import kireiiiiiiii.ui.Background;
import kireiiiiiiii.ui.ConnectionWidget;
import kireiiiiiiii.ui.Player;
import kireiiiiiiii.util.ScreenUtil;

/**
 * Main class for the LAN Game application.
 */
public class App implements InteractableHandeler {

    /////////////////
    // Constants
    ////////////////

    public static final int TCP_PORT = 54321;
    private static final int FPS = 60;
    private static final String IP_FILE = "ip.txt";

    /////////////////
    // Variables
    ////////////////

    private Client client;
    private String username;
    private GPanel gpanel;
    private int[] pos;
    private int[] windowSize;
    private int[] initPos;

    /////////////////
    // Main & Constructor
    ////////////////

    /**
     * Main method.
     * 
     * @param args
     */
    public static void main(String[] args) {
        Logger.clearLog();
        Logger.addLog("App started");
        SwingUtilities.invokeLater(App::new);
    }

    /**
     * Constructor for the application.
     * 
     */
    public App() {

        // ------ Get IP
        String ip = getFirstLine(IP_FILE);
        InetAddress serverAddress;
        if (ip != null) {
            try {
                serverAddress = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                Logger.addLog("Unknown host exception when trying to get IP from a file.", e);
                serverAddress = Client.getIPv4Address();
                Logger.addLog("No " + IP_FILE + ". Detected ip. Using local IP adress");
            }
            Logger.addLog("Found " + IP_FILE + ". Detected ip: " + ip);
        } else {
            serverAddress = Client.getIPv4Address();
            Logger.addLog("No " + IP_FILE + ". Detected ip. Using local IP adress");
        }

        // ------ Check if server is running
        if (!Client.isServerRunning(serverAddress, TCP_PORT)) {
            System.out.println("Cannot reach server.");
            Logger.addLog("Couldn't reach the server - server is running. Exiting...");
            System.exit(1);
        }

        // ------ Initialize game
        this.windowSize = ScreenUtil.getScreenDimensions();
        this.pos = new int[2];
        this.pos[0] = 0;
        this.pos[1] = 0;
        this.initPos = new int[] { this.windowSize[0] / 2, this.windowSize[1] / 2 };
        Scanner console = new Scanner(System.in);
        printHeader();

        // ------ Get username
        System.out.print("\nEnter your name: ");
        this.username = console.nextLine();
        Logger.addLog("User entered name: " + username);

        // ------ Initialize client & UI
        this.client = new Client(username, serverAddress);
        this.gpanel = new GPanel(this, FPS, this.windowSize[0], this.windowSize[1], false, false,
                "Tournament Tournament Tournament");
        initializeWidgets();
        Logger.addLog("UI window created");

        // ------ Start game loop
        GameLoop gameLoop = new GameLoop(FPS);
        gameLoop.start();
        Logger.addLog("Game loop started");

        // ------ Bottom of main
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
        gpanel.add(
                new Player(new int[] { this.initPos[0], this.initPos[1] }, Color.BLUE, ZIndex.PLAYER, username, false));
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

        // Update viewport
        this.gpanel.setPosition(new int[] { -this.pos[0] + initPos[0], -this.pos[1] + initPos[1] });
    }

    /////////////////
    // Helper methods
    ////////////////

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
     * Gets the first line of a {@code .txt} file.
     * 
     * @param filePath - {@code String} of the file path.
     * @return - a {@code String} of the first line of the file.
     */
    public static String getFirstLine(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    /////////////////
    // Game loop class
    ////////////////

    /**
     * Game loop for the application.
     * 
     */
    @SuppressWarnings("unused")
    private class GameLoop implements Runnable {

        private boolean running = false;
        private int targetFPS;

        /**
         * Gameloop constructor. WILL NOT START THE GAME LOOP AUTOMATICALLY!!
         * 
         * @param fps
         */
        public GameLoop(int fps) {
            setFps(fps);
        }

        /**
         * Starts the rendering loop.
         * 
         */
        public void start() {
            running = true;
            new Thread(this, "Game loop Thread").start();
        }

        /**
         * Stops the rendering loop.
         * 
         */
        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                long optimalTime = 1_000_000_000 / targetFPS;
                long startTime = System.nanoTime();

                update();

                long elapsedTime = System.nanoTime() - startTime;
                long sleepTime = optimalTime - elapsedTime;

                if (sleepTime > 0) {
                    try {
                        Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        /**
         * Update method that will pull player data from the server, and display it to
         * the user.
         * 
         */
        private void update() {
            gpanel.removeWidgetsOfClass(ConnectionWidget.class);

            ArrayList<ClientStruct> positions = client
                    .updatePlayerPositions(new ClientStruct(pos[0], pos[1], username));
            ArrayList<Renderable> players = new ArrayList<Renderable>();

            // gpanel.removeWidgetsOfClass(Player.class);
            gpanel.removeWidgetsWithTags(WidgetTags.ENEMY_PLAYER);

            for (ClientStruct client : positions) {
                int[] clientPosition = { client.getX(), client.getY() };
                players.add(new Player(clientPosition, Color.RED, ZIndex.OTHER_PLAYERS, client.getName(), true));
            }

            gpanel.add(players);
            gpanel.add(new ConnectionWidget());
        }

        /**
         * Set a new FPS value.
         * 
         * @param value - new FPS value.
         */
        public void setFps(int value) {
            targetFPS = value;
        }
    }
}
