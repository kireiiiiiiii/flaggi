/*
 * Author: Matěj Šťastný
 * Date created: 4/11/2024
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

/**
 * Main class for the LAN Game application.
 */
public class App implements InteractableHandeler {

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
        SwingUtilities.invokeLater(App::new);
    }

    public App() {

        try (ServerSocket serverSocket = new ServerSocket(Client.TCP_PORT)) {
            // serverSocket.close();
            System.out.println("Server not running");
            System.exit(1);
        } catch (IOException e) {
        }

        Scanner console = new Scanner(System.in);
        printHeader();
        System.out.print("\nEnter your name: ");
        String username = console.nextLine();

        System.out.println("Before servers");
        client = new Client(username); // Use username for client

        this.pos[0] = 250;
        this.pos[1] = 250;
        this.gpanel = new GPanel(this, 60, 500, 500, false, "LAN test");
        this.gpanel.startRendering();
        this.gpanel.add(new Player(pos, Color.GREEN));

        GameLoop gl = new GameLoop(60);
        gl.start();
        console.close();
    }

    /////////////////
    // Events
    ////////////////

    public void updatePos(int x, int y) {
        pos[0] = x;
        pos[1] = y;
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
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP: // Up Arrow Key
            case KeyEvent.VK_W: // W key
                this.pos[1] -= 10; // Move up (increase Y)
                break;
            case KeyEvent.VK_DOWN: // Down Arrow Key
            case KeyEvent.VK_S: // S key
                this.pos[1] += 10; // Move down (decrease Y)
                break;
            case KeyEvent.VK_LEFT: // Left Arrow Key
            case KeyEvent.VK_A: // A key
                this.pos[0] -= 10; // Move left (decrease X)
                break;
            case KeyEvent.VK_RIGHT: // Right Arrow Key
            case KeyEvent.VK_D: // D key
                this.pos[0] += 10; // Move right (increase X)
                break;
            default:
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    private void printHeader() {
        System.out.println("\n" +
                "     ____.  _________   _________    .____       _____    _______   \n" +
                "    |    | /  _  \\   \\ /   /  _  \\   |    |     /  _  \\   \\      \\  \n" +
                "    |    |/  /_\\  \\   Y   /  /_\\  \\  |    |    /  /_\\  \\  /   |   \\ \n" +
                "/\\__|    /    |    \\     /    |    \\ |    |___/    |    \\/    |    \\\n" +
                "\\________\\____|__  /\\___/\\____|__  / |_______ \\____|__  /\\____|__  /\n" +
                "                 \\/              \\/          \\/       \\/         \\/ \n");
    }

    private static boolean getYesNoInput(String message, Scanner console) {
        while (true) {
            System.out.print(message + " (y/n): ");
            String input = console.nextLine().trim().toLowerCase();
            if (input.equals("y")) {
                return true;
            } else if (input.equals("n")) {
                return false;
            } else {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }
    }

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
                long optimalTime = 1_000_000_000 / targetFPS; // In nanoseconds
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
            System.out.println("before");
            ArrayList<int[]> positions = client.sendPos(pos[0], pos[1]);
            System.out.println(pos[0] + pos[1]); // ! TODO DELETE
            gpanel.removeAll();
            for (int[] position : positions) {
                gpanel.add(new Player(position, Color.RED));
            }
            gpanel.add(new Player(pos, Color.BLUE)); // Add the local player
        }

        public void setFps(int value) {
            targetFPS = value;
        }
    }
}
