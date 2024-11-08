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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
    private static final String IP_FILE = "ip.txt";

    /////////////////
    // Variables
    ////////////////

    private Client client;
    private final int[] pos = { 250, 250 }; // Initialize player position
    private String username;
    private GPanel gpanel;

    /////////////////
    // Main & Constructor
    ////////////////

    public static void main(String[] args) {
        Logger.addLog("App started", false);
        SwingUtilities.invokeLater(App::new);
    }

    public App() {

        // ------ Get IP
        String ip = getFirstLine(IP_FILE);
        InetAddress serverAddress;
        if (ip != null) {
            try {
                serverAddress = InetAddress.getByName(ip);
            } catch (UnknownHostException e) {
                Logger.addLog("Unknown host exception when trying to get IP from a file.", e, true);
                serverAddress = Client.getIPv4Address();
                Logger.addLog("No " + IP_FILE + ". Detected ip. Using local IP adress", true);
            }
            Logger.addLog("Found " + IP_FILE + ". Detected ip: " + ip, true);
        } else {
            serverAddress = Client.getIPv4Address();
            Logger.addLog("No " + IP_FILE + ". Detected ip. Using local IP adress", true);
        }

        // ------ Check if server is running
        if (!isServerRunning(serverAddress, TCP_PORT)) {
            System.out.println("Cannot reach server.");
            Logger.addLog("Coudn't reach the server - server is running. Exiting...",
                    true);
            System.exit(1);
        }

        // ------ Initialize game
        this.pos[0] = 250;
        this.pos[1] = 250;
        Scanner console = new Scanner(System.in);
        printHeader();

        System.out.print("\nEnter your name: ");
        this.username = console.nextLine();
        Logger.addLog("User entered name: " + username, true);

        client = new Client(username, serverAddress);

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
     * Checks if there is a server running on a specific IP address and port by
     * trying to establish a connection.
     * 
     * @param serverAddress - target IP address or hostname as a String
     * @param port          - target port
     * @return boolean value, true if something is running on the specified IP and
     *         port
     */
    private static boolean isServerRunning(InetAddress serverAddress, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverAddress, port), 2000); // 2-second timeout
            // Immediately close after connection to avoid interfering with server
            // operations
            return true;
        } catch (IOException e) {
            Logger.addLog("Server running check failed.", e, true);
            return false;
        }
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

        /**
         * Update method that will pull player data from the server, and display it to
         * the user.
         * 
         */
        private void update() {
            ArrayList<ClientStruct> positions = client
                    .updatePlayerPositions(new ClientStruct(pos[0], pos[1], username));
            ArrayList<Renderable> players = new ArrayList<Renderable>();

            gpanel.removeWidgetsOfClass(Player.class);
            players.add(new Player(pos, Color.BLUE, ZIndex.PLAYER, ""));

            for (ClientStruct client : positions) {
                int[] clientPosition = { client.getX(), client.getY() };
                players.add(new Player(clientPosition, Color.RED, ZIndex.OTHER_PLAYERS, client.getName()));
            }

            gpanel.add(players);
        }

        public void setFps(int value) {
            targetFPS = value;
        }
    }
}
