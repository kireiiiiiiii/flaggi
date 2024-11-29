/*
 * Author: Matěj Šťastný
 * Date created: 11/4/2024
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

package flaggi;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import flaggi.common.AdvancedVariable;
import flaggi.common.AppOptions;
import flaggi.common.Client;
import flaggi.common.GPanel;
import flaggi.common.Logger;
import flaggi.common.Client.ClientStruct;
import flaggi.common.GPanel.Interactable;
import flaggi.common.GPanel.InteractableHandeler;
import flaggi.common.GPanel.Renderable;
import flaggi.common.GPanel.Typable;
import flaggi.constants.WidgetTags;
import flaggi.constants.ZIndex;
import flaggi.ui.Background;
import flaggi.ui.ConnectionWidget;
import flaggi.ui.MenuScreen;
import flaggi.ui.PauseMenu;
import flaggi.ui.Player;
import flaggi.ui.Tree;
import flaggi.util.ImageUtil;
import flaggi.util.ScreenUtil;

/**
 * Main class for the LAN Game application.
 */
public class App implements InteractableHandeler {

    /////////////////
    // Constants
    ////////////////

    public static final String PROJECT_NAME = "Flaggi";
    public static final String DATA_DIRECTORY_NAME = "kireiiiiiiii.flaggi";
    public static final int TCP_PORT = 54321;
    private static final int FPS = 60;

    /////////////////
    // Variables
    ////////////////

    private Client client;
    private String username, ip;
    private GPanel gpanel;
    private GameLoop gameLoop;
    private AdvancedVariable<AppOptions> appOptions;
    private ArrayList<KeyEvent> pressedKeys;
    private int[] pos, initPos, windowSize;
    private boolean movementEnabled, paused;

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

        // ------ Initialize game
        try {
            this.appOptions = new AdvancedVariable<AppOptions>(
                    getApplicationDataFolder() + File.separator + "user-options.json", AppOptions.class);
        } catch (IOException e) {
            Logger.addLog("Error loading app options.", e);
            this.appOptions.set(getDefaultOptions());
        }
        if (this.appOptions.get() == null) {
            this.appOptions.set(getDefaultOptions());
        }

        this.windowSize = ScreenUtil.getScreenDimensions();
        this.pos = new int[2];
        this.pos[0] = 0;
        this.pos[1] = 0;
        this.initPos = new int[] { this.windowSize[0] / 2, this.windowSize[1] / 2 };
        this.movementEnabled = false;
        this.paused = false;
        this.pressedKeys = new ArrayList<KeyEvent>();
        printHeader();

        // ------ Initialize UI
        this.gpanel = new GPanel(this, FPS, this.windowSize[0], this.windowSize[1], false, false, PROJECT_NAME);
        this.gpanel.setIcon(ImageUtil.getImageFromFile("icon.png"));
        this.gpanel.setExitOperation(() -> {
            exitGame();
        });
        initializeWidgets();
        Logger.addLog("UI window created");
        goToMenu();

    }

    /////////////////
    // Events
    ////////////////

    public void startGame() {
        // ------ Get username
        for (MenuScreen m : this.gpanel.getWidgetsByClass(MenuScreen.class)) {
            this.username = m.getName();
        }
        String userNameValidation = isValidUsername(this.username);
        if (userNameValidation != null) {
            for (MenuScreen m : this.gpanel.getWidgetsByClass(MenuScreen.class)) {
                m.setErrorMessage(userNameValidation);
            }
            return;
        }
        Logger.addLog("User entered name '" + username + "'");

        // ------ Get IP
        this.ip = "";
        for (MenuScreen m : this.gpanel.getWidgetsByClass(MenuScreen.class)) {
            ip = m.getIP();
        }
        if (ip.length() < 1) {
            for (MenuScreen m : this.gpanel.getWidgetsByClass(MenuScreen.class)) {
                m.setErrorMessage("No server IP entered.");
            }
            return;
        }

        InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            Logger.addLog("Unknown host exception when trying to get IP from a file.",
                    e);
            return;
        }
        Logger.addLog("Selected ip: " + ip);

        // ------ Check if server is running
        if (!Client.isServerRunning(serverAddress, TCP_PORT)) {
            for (MenuScreen m : this.gpanel.getWidgetsByClass(MenuScreen.class)) {
                m.setErrorMessage("Not a valid IP. Please try again.");
            }
            return;
        }

        // ------ Initialize client & change UI
        this.client = new Client(username, serverAddress);
        this.gpanel.add(new Player(new int[] { this.initPos[0], this.initPos[1] }, Color.BLUE, ZIndex.PLAYER, username,
                false));

        this.gpanel.hideAllWidgets();
        this.gpanel.showTaggedWidgets(WidgetTags.GAME_ELEMENTS);

        // ------ Start game loop
        this.movementEnabled = true;
        this.gameLoop = new GameLoop(FPS);
        this.gameLoop.start();
        Logger.addLog("Game loop started");

    }

    /**
     * Event done on exit the game.
     * 
     */
    public void exitGame() {
        try {
            if (this.username != null && this.ip != null) {
                this.appOptions.set(new AppOptions(this.username, this.ip));
            }
            this.appOptions.save();
            Logger.addLog("Menu fields data saved succesfully.");
        } catch (IOException e) {
            Logger.addLog("Saving menu fields data not succesful.", e);
        }
        System.exit(1);
    }

    /**
     * Toggles the game pause menu.
     * 
     */
    public void togglePauseMenu() {
        if (this.paused) {
            this.gpanel.showTaggedWidgets(WidgetTags.PAUSE_MENU);
        } else {
            this.gpanel.hideTaggedWidgets(WidgetTags.PAUSE_MENU);
        }

        this.paused = !paused;
    }

    /**
     * Returns the game back to the menu.
     * 
     */
    public void goToMenu() {
        if (this.gameLoop != null) {
            this.gameLoop.stop();
        }
        movementEnabled = false;
        paused = false;
        this.pos[0] = 0;
        this.pos[1] = 0;

        // ------ Initialize UI
        this.gpanel.hideAllWidgets();
        this.gpanel.showTaggedWidgets(WidgetTags.MENU_ELEMENTS);
        this.gpanel.setPosition(new int[] { -this.pos[0] + initPos[0], -this.pos[1] + initPos[1] });
        Logger.addLog("Menu screen active.");
    }

    /**
     * Moves the player.
     * 
     * @param e
     */
    public void move(KeyEvent e) {
        int step = 8;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                this.pos[1] -= step;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                this.pos[1] += step;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                this.pos[0] -= step;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                this.pos[0] += step;
                break;
            default:
                break;
        }

        // Update viewport
        this.gpanel.setPosition(new int[] { -this.pos[0] + initPos[0], -this.pos[1] + initPos[1] });
    }

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
        ArrayList<Renderable> widgets = new ArrayList<Renderable>(Arrays.asList(
                new Background(),
                new MenuScreen(() -> {
                    startGame();
                }, this.appOptions.get().name, this.appOptions.get().ip),
                new PauseMenu(() -> {
                    togglePauseMenu();
                }, () -> {
                    goToMenu();
                }),
                new Tree()));

        // Add all the widgets
        this.gpanel.add(widgets);
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
        for (Renderable r : gpanel.getInteractables()) {
            Interactable i = (Interactable) r;
            i.interact(e);
        }
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
        for (KeyEvent ke : this.pressedKeys) {
            if (ke.getKeyCode() == e.getKeyCode()) {
                this.pressedKeys.remove(ke);
                break;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!this.movementEnabled) {
            for (Renderable r : gpanel.getTypables()) {
                Typable t = (Typable) r;
                t.type(e);
            }
        }

        // Check for duplication
        for (KeyEvent ke : this.pressedKeys) {
            if (ke.getKeyCode() == e.getKeyCode()) {
                return;
            }
        }
        this.pressedKeys.add(e);

        // Exceptions
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ESCAPE:
                togglePauseMenu();
                return;
            default:
                break;
        }

    }

    /////////////////
    // Helper methods
    ////////////////

    /**
     * Prints the header of this game
     * 
     */
    private void printHeader() {
        System.out.println("\n" + //
                " ______   __         ______     ______     ______     __    \n" +
                "/\\  ___\\ /\\ \\       /\\  __ \\   /\\  ___\\   /\\  ___\\   /\\ \\   \n" +
                "\\ \\  __\\ \\ \\ \\____  \\ \\  __ \\  \\ \\ \\__ \\  \\ \\ \\__ \\  \\ \\ \\  \n" +
                " \\ \\_\\    \\ \\_____\\  \\ \\_\\ \\_\\  \\ \\_____\\  \\ \\_____\\  \\ \\_\\ \n" +
                "  \\/_/     \\/_____/   \\/_/\\/_/   \\/_____/   \\/_____/   \\/_/ \n" +
                "                                                            \n" +
                "");
    }

    /**
     * Checks if a username is a valid one (if it can exist, not if it exists).
     * 
     * @param username - target username.
     * @return {@code String} of the error message if the username is invalid,
     *         otherwise {@code null}.
     */
    private static String isValidUsername(String username) {
        if (username == null) {
            return "Username null.";
        } else if (username.length() < 1) {
            return "Username too short.";
        }
        return null;
    }

    /**
     * Gets the program Application Data Folder path. If it doesn't exist, it will
     * create one.
     * 
     * @return - {@code String} of the application data folder path.
     */
    private static String getApplicationDataFolder() {
        String os = System.getProperty("os.name").toLowerCase();
        String appDataFolder;

        if (os.contains("win")) {
            // Windows: Use %APPDATA%
            appDataFolder = System.getenv("APPDATA");
        } else if (os.contains("mac")) {
            // macOS: Use ~/Library/Application Support/
            appDataFolder = System.getProperty("user.home") + File.separator + "Library" + File.separator
                    + "Application Support";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            // Linux/Unix: Use ~/.config/
            appDataFolder = System.getProperty("user.home") + File.separator + ".config";
        } else {
            // Other: Use the directory of server jar.
            appDataFolder = File.separator;
        }

        // Add the application's specific folder name
        appDataFolder = appDataFolder + File.separator + DATA_DIRECTORY_NAME;

        // Ensure the directory exists
        File folder = new File(appDataFolder);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create application data folder at: " + appDataFolder);
            }
        }

        return appDataFolder;
    }

    /**
     * Returns the default menu options.
     * 
     * @return - a {@code AppOptions} object.
     */
    private AppOptions getDefaultOptions() {
        return new AppOptions("", "");
    }

    /////////////////
    // Game loop class
    ////////////////

    /**
     * Game loop for the application.
     * 
     */
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
            if (movementEnabled) {
                for (KeyEvent e : pressedKeys) {
                    move(e);
                }
            }

            gpanel.removeWidgetsOfClass(ConnectionWidget.class);

            ArrayList<ClientStruct> positions = client
                    .updatePlayerPositions(new ClientStruct(pos[0], pos[1], username));
            ArrayList<Renderable> players = new ArrayList<Renderable>();

            // gpanel.removeWidgetsOfClass(Player.class);
            gpanel.removeWidgetsWithTags(WidgetTags.ENEMY_PLAYER);

            for (ClientStruct client : positions) {
                int[] clientPosition = { client.getX(), client.getY() };

                // Cull clients out of viewport
                int distanceX = Math.abs(clientPosition[0] - pos[0]);
                int distanceY = Math.abs(clientPosition[1] - pos[1]);
                if (distanceX < windowSize[0] / 1.5 && distanceY < windowSize[1] / 1.5) {
                    players.add(new Player(clientPosition, Color.RED, ZIndex.OTHER_PLAYERS, client.getName(), true));
                }
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
