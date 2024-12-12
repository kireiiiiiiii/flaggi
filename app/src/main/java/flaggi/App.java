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
import java.util.HashMap;
import java.util.List;

import javax.swing.SwingUtilities;

import flaggi.common.AdvancedVariable;
import flaggi.common.AppOptions;
import flaggi.common.Client;
import flaggi.common.GPanel;
import flaggi.common.Logger;
import flaggi.common.Client.ClientStruct;
import flaggi.common.Client.RecievedServerDataStruct;
import flaggi.common.GPanel.Interactable;
import flaggi.common.GPanel.InteractableHandeler;
import flaggi.common.GPanel.Renderable;
import flaggi.common.GPanel.Typable;
import flaggi.constants.WidgetTags;
import flaggi.ui.Background;
import flaggi.ui.Bullet;
import flaggi.ui.ConnectionWidget;
import flaggi.ui.HUD;
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
    public static final String FILE_JAR_SEPARATOR = "/";
    public static final Logger LOGGER = Logger.getLogger(getApplicationDataFolder() + File.separator + "logs" + File.separator + "app.log");
    public static final int TCP_PORT = 54321;
    public static final int FPS = 60;

    /////////////////
    // Variables
    ////////////////

    private Client client;
    private String username, ip;
    private int id, health;
    private GPanel gpanel;
    private GameLoop gameLoop;
    private AdvancedVariable<AppOptions> appOptions;
    private ArrayList<KeyEvent> pressedKeys;
    private ArrayList<Bullet> quedPlayerObjects;
    private Player localPlayer;
    private int[] pos, spawnPoint, windowSize;
    private boolean movementEnabled, paused;

    /////////////////
    // Main & Constructor
    ////////////////

    /**
     * Main method.
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(App::new);
    }

    /**
     * Constructor for the application.
     * 
     */
    public App() {

        // ------ Clear the log file, and start the 1st log entry.
        LOGGER.clearLog();
        LOGGER.addLog("App started");

        // ------ Initialize game
        try {
            this.appOptions = new AdvancedVariable<AppOptions>(getApplicationDataFolder() + File.separator + "user-options.json", AppOptions.class);
        } catch (IOException e) {
            LOGGER.addLog("Error loading app options.", e);
            new File(getApplicationDataFolder() + File.separator + "user-options.json").delete();
            this.appOptions = new AdvancedVariable<AppOptions>(getApplicationDataFolder() + File.separator + "user-options.json");
            this.appOptions.set(getDefaultOptions());
            try {
                this.appOptions.save();
            } catch (IOException e1) {
            }
        }

        if (this.appOptions.get() == null) {
            this.appOptions.set(getDefaultOptions());
        }

        this.windowSize = ScreenUtil.getScreenDimensions();
        this.pos = new int[2];
        this.pos[0] = 0;
        this.pos[1] = 0;
        this.spawnPoint = new int[] { this.windowSize[0] / 2, this.windowSize[1] / 2 };
        this.movementEnabled = false;
        this.paused = false;
        this.health = 69;
        this.pressedKeys = new ArrayList<KeyEvent>();
        this.quedPlayerObjects = new ArrayList<Bullet>();
        printHeader();

        // ------ Initialize UI
        this.gpanel = new GPanel(this, FPS, this.windowSize[0], this.windowSize[1], false, PROJECT_NAME, new Color(229, 204, 255));
        this.gpanel.setIconOSDependend(ImageUtil.getImageFromFile("icon_win.png"), ImageUtil.getImageFromFile("icon_mac.png"), ImageUtil.getImageFromFile("icon_win.png"), ImageUtil.getImageFromFile("icon_win.png"));
        this.gpanel.setExitOperation(() -> {
            exitGame();
        });
        initializeWidgets();
        LOGGER.addLog("UI window created");
        goToMenu();

    }

    /////////////////
    // Events
    ////////////////

    /**
     * Starts the game (not the app).
     * 
     */
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
        LOGGER.addLog("User entered name '" + username + "'");

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
            LOGGER.addLog("Unknown host exception when trying to get IP from a file.", e);
            return;
        }
        LOGGER.addLog("Selected ip: " + ip);

        // ------ Check if server is running
        if (!Client.isServerRunning(serverAddress, TCP_PORT)) {
            for (MenuScreen m : this.gpanel.getWidgetsByClass(MenuScreen.class)) {
                m.setErrorMessage("Not a valid IP. Please try again.");
            }
            return;
        }

        // ------ Set the skin name
        String skinName = Player.DEFAULT_SKIN; // default skin
        if (username.equals("daarlin") || username.equals("owo")) {
            skinName = "venom";
        } else if (username.equals("snapshot")) {
            skinName = "jester";
        }

        // ------ Initialize client & change UI
        this.client = new Client(username, serverAddress);
        this.id = this.client.getId();
        this.localPlayer = new Player(new int[] { this.spawnPoint[0], this.spawnPoint[1] }, username, skinName, this.id);
        this.gpanel.add(this.localPlayer);

        this.gpanel.hideAllWidgets();
        this.gpanel.showTaggedWidgets(WidgetTags.GAME_ELEMENTS);

        // ------ Start game loop
        this.movementEnabled = true;
        this.gameLoop = new GameLoop(FPS);
        this.gameLoop.start();
        LOGGER.addLog("Game loop started");

    }

    /**
     * Event done on exit the game.
     * 
     */
    public void exitGame() {
        if (this.gameLoop != null)
            this.gameLoop.stop();
        if (this.client != null)
            this.client.disconnectFromServer();
        try {
            if (this.username != null && this.ip != null) {
                this.appOptions.set(new AppOptions(this.username, this.ip));
            }
            this.appOptions.save();
            LOGGER.addLog("Menu fields data saved succesfully.");
        } catch (IOException e) {
            LOGGER.addLog("Saving menu fields data not succesful.", e);
        }
        System.exit(1);
    }

    /**
     * Toggles the game pause menu.
     * 
     */
    public void togglePauseMenu() {
        this.paused = !this.paused;
        this.movementEnabled = !this.paused;
        if (this.paused) {
            this.gpanel.showTaggedWidgets(WidgetTags.PAUSE_MENU);
        } else {
            this.gpanel.hideTaggedWidgets(WidgetTags.PAUSE_MENU);
        }
    }

    /**
     * Returns the game back to the menu.
     * 
     */
    public void goToMenu() {

        // ------ Clear the menu screen error message
        for (MenuScreen m : this.gpanel.getWidgetsByClass(MenuScreen.class)) {
            m.clearErrorMessage();
        }

        // ------ Reset variables
        if (this.client != null)
            this.client.disconnectFromServer();
        if (this.gameLoop != null)
            this.gameLoop.stop();
        movementEnabled = false;
        paused = false;
        this.gpanel.remove(this.localPlayer);
        this.localPlayer = null;
        this.pos[0] = 0;
        this.pos[1] = 0;

        // ------ Initialize UI
        this.gpanel.hideAllWidgets();
        this.gpanel.showTaggedWidgets(WidgetTags.MENU_ELEMENTS);
        this.gpanel.setCameraPosition(new int[] { -this.pos[0] + spawnPoint[0], -this.pos[1] + spawnPoint[1] });
        LOGGER.addLog("Menu screen active.");
    }

    /**
     * Moves the player.
     * 
     * @param inputEvents - {@code List<KeyEvent>} of the pressed keys.
     */
    public void move(List<KeyEvent> inputEvents) {
        int step = 8;
        boolean moveUp = false, moveDown = false, moveLeft = false, moveRight = false;
        List<KeyEvent> events = new ArrayList<>();
        events.addAll(inputEvents);

        for (KeyEvent e : inputEvents) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                moveUp = true;
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                moveDown = true;
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                moveLeft = true;
                break;
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                moveRight = true;
                break;
            default:
                break;
            }
        }

        // Decide on sprite direction
        if (moveRight) {
            this.localPlayer.setFacingRight(true);
        } else if (moveLeft) {
            this.localPlayer.setFacingRight(false);
        }

        // Switch correct walkig animations
        if ((moveUp && moveLeft) || (moveUp && moveRight)) {
            this.localPlayer.switchAnimation("walk_diagup");
        } else if ((moveDown && moveLeft) || (moveDown && moveRight)) {
            this.localPlayer.switchAnimation("idle"); // TODO ADD ANIMATIONS
        } else if (moveUp) {
            // TODO ADD ANIMATIONS
        } else if (moveDown) {
            // TODO ADD ANIMATIONS
        } else if (moveLeft) {
            this.localPlayer.switchAnimation("walk_side");
        } else if (moveRight) {
            this.localPlayer.switchAnimation("walk_side");
        }

        // Calculate horizontal and vertical movement distances.
        double deltaX = 0;
        double deltaY = 0;

        if (moveUp)
            deltaY -= 1;
        if (moveDown)
            deltaY += 1;
        if (moveLeft)
            deltaX -= 1;
        if (moveRight)
            deltaX += 1;

        // Diagonal movement handeling
        double magnitude = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (magnitude > 0) {
            deltaX = (deltaX / magnitude) * step;
            deltaY = (deltaY / magnitude) * step;
        }

        // Update the player's position
        this.pos[0] += deltaX;
        this.pos[1] += deltaY;

        // Update the viewport
        this.gpanel.setCameraPosition(new int[] { -this.pos[0] + this.spawnPoint[0], -this.pos[1] + this.spawnPoint[1] });
    }

    /**
     * Shoots a bullet from the player.
     * 
     * @param e - {@code MouseEvent} of the mouse click.
     */
    public void shoot(MouseEvent e) {
        Bullet b = new Bullet(this.pos, getMouseclickLocationRelativeToMap(e), 500, 1000, this.id);
        Runnable afterDecay = () -> {
            this.gpanel.remove(b);
        };
        b.setAfterDecayRunnable(afterDecay);
        this.quedPlayerObjects.add(b);
    }

    /**
     * Gets the player informations from the server, and updates the player data
     * according to that. Removes any players no longer connected, and adds new
     * players.
     * 
     */
    public void updatePlayerData() {
        String localAnimationFrame = null;
        for (Player player : this.gpanel.getWidgetsByClass(Player.class)) {
            if (!player.isEnemy()) {
                localAnimationFrame = player.getAnimationFrame();
            }
        }

        // Get the current players from the panel and their positions from the server
        ArrayList<Player> players = this.gpanel.getWidgetsByClass(Player.class);
        RecievedServerDataStruct struct = client.updatePlayerPositions(new ClientStruct(pos[0], pos[1], this.id, this.health, this.username, localAnimationFrame, getPlayerObjectDataString(true)));
        List<ClientStruct> serverPositions = struct.connectedClientsList;
        String playerObjectData = struct.playerObjectData;

        // Get the local player, and remove it from the rendering list
        ClientStruct localPlayerStruct = null;
        for (ClientStruct cs : serverPositions) { // Find the local player
            if (cs.getId() == this.id) {
                localPlayerStruct = cs;
                serverPositions.remove(localPlayerStruct); // Remove local player for rendering
                break;
            }
        }

        // ---- LOCAL PLAYER DATA SET

        if (localPlayerStruct != null) {
            // Health
            this.health = localPlayerStruct.getHealth();
            this.localPlayer.setHealth(this.health);
            for (HUD hud : this.gpanel.getWidgetsByClass(HUD.class)) {
                hud.setHealth((int) health);
            }

        }

        // --- PLAYER OBJECT HANDELING
        updatePlayerObjects(playerObjectData);

        // Track existing players by ID for quick lookup
        HashMap<Integer, Player> existingPlayers = new HashMap<>();
        for (Player player : players) {
            existingPlayers.put(player.getId(), player);
        }

        // Culling distance calculation
        int viewportWidth = windowSize[0] + 100; // Add 100 so the player wont dissapear at the edge of the screen
        int viewportHeight = windowSize[1] + 100;
        int cullDistanceX = viewportWidth / 2;
        int cullDistanceY = viewportHeight / 2;

        // Update or add players
        for (ClientStruct clientStruct : serverPositions) {
            int clientId = clientStruct.getId();
            int[] clientPos = new int[] { clientStruct.getX(), clientStruct.getY() };
            int health = clientStruct.getHealth();
            String animationFrame = clientStruct.getAnimationFrame();

            // Cull clients outside the viewport
            int distanceX = Math.abs(clientPos[0] - pos[0]);
            int distanceY = Math.abs(clientPos[1] - pos[1]);
            if (distanceX > cullDistanceX || distanceY > cullDistanceY) {
                continue;
            }

            if (existingPlayers.containsKey(clientId)) {
                // Update the position of the existing player
                Player player = existingPlayers.get(clientId);
                player.setPos(clientPos);
                player.setHealth(health);
                player.setAnimationFrameData(animationFrame);
                existingPlayers.remove(clientId); // Mark as processed
            } else {
                // Add new player to the panel
                Player newPlayer = new Player(clientPos, clientStruct.getName(), clientId, animationFrame);
                this.gpanel.add(newPlayer);
            }
        }

        // Remove players no longer reported by the server
        for (Player remainingPlayer : existingPlayers.values()) {
            // Exclude the local player
            if (remainingPlayer.getId() == this.id) {
                continue;
            }
            this.gpanel.remove(remainingPlayer);
        }
    }

    /**
     * Initializes all new widgets and adds them to the gpanel.
     * 
     */
    public void initializeWidgets() {
        ArrayList<Renderable> widgets = new ArrayList<Renderable>(Arrays.asList(new Background(), new MenuScreen(() -> {
            startGame();
        }, this.appOptions.get().name, this.appOptions.get().ip), new PauseMenu(() -> {
            togglePauseMenu();
        }, () -> {
            goToMenu();
        }), new Tree(new int[] { 100, 100 }), new HUD()));

        // Add all the widgets
        this.gpanel.add(widgets);
    }

    /**
     * Updates the positions of the player objects.
     * 
     * @param playerObjectData - enemy created object data.
     */
    public void updatePlayerObjects(String playerObjectData) {

        String newObjectData;
        String oldObjectData;

        if (playerObjectData.startsWith("/") && playerObjectData.length() > 1) {
            newObjectData = null;
            oldObjectData = playerObjectData.substring(1);
        } else if (playerObjectData.length() <= 1) {
            newObjectData = null;
            oldObjectData = null;
        } else if (playerObjectData.endsWith("/")) {
            String[] splitData = playerObjectData.split("/");
            newObjectData = splitData.length > 0 ? splitData[0] : null;
            oldObjectData = null;
        } else {
            String[] splitData = playerObjectData.split("/");
            newObjectData = splitData.length > 0 ? splitData[0] : null;
            oldObjectData = splitData.length > 1 ? splitData[1] : null;
        }

        // ---- Add new objects
        if (newObjectData != null) { // If empty just dont do anything
            String[] newObjects = newObjectData.split(",");
            for (String object : newObjects) {
                String[] objectData = object.split(":");
                if (objectData.length != 6) {
                    continue;
                }

                String bulletId = objectData[1];
                int[] initPos = Arrays.stream(objectData[2].split("&")).mapToInt(Integer::parseInt).toArray();
                int[] targetPos = Arrays.stream(objectData[3].split("&")).mapToInt(Integer::parseInt).toArray();
                int decayTime = Integer.parseInt(objectData[4]);
                int initVelocity = Integer.parseInt(objectData[5]);

                Bullet b = new Bullet(initPos, targetPos, initVelocity, decayTime, bulletId);
                this.gpanel.add(b);
            }
        }

        List<Bullet> bullets = this.gpanel.getWidgetsByClass(Bullet.class);

        if (oldObjectData != null) { // If empty remove all
            String[] oldObjects = oldObjectData.split(",");
            for (String object : oldObjects) {
                synchronized (bullets) {
                    List<Bullet> bulletsToRemove = new ArrayList<>();
                    for (Bullet b : bullets) {
                        if (object.equals(b.getObjectId())) {
                            bulletsToRemove.add(b); // Collect bullets to remove
                        }
                    }
                    for (Bullet b : bulletsToRemove) {
                        bullets.remove(b); // Remove them after iteration
                    }
                }

            }
        }

        for (Bullet b : bullets) {
            this.gpanel.remove(b);
        }

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
        if (this.movementEnabled) {
            shoot(e);
        }
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
        System.out.println("\n" + " ______   __         ______     ______     ______     __    \n" + "/\\  ___\\ /\\ \\       /\\  __ \\   /\\  ___\\   /\\  ___\\   /\\ \\   \n" + "\\ \\  __\\ \\ \\ \\____  \\ \\  __ \\  \\ \\ \\__ \\  \\ \\ \\__ \\  \\ \\ \\  \n" + " \\ \\_\\    \\ \\_____\\  \\ \\_\\ \\_\\  \\ \\_____\\  \\ \\_____\\  \\ \\_\\ \n" + "  \\/_/     \\/_____/   \\/_/\\/_/   \\/_____/   \\/_____/   \\/_/ \n" + "                                                            \n" + "");
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
     * Calculates the mouse click position relative to the map (the position of the
     * mouse clikc on the world map).
     * 
     * @param e - {@code MouseEvent} of the mouse click.
     * @return 2D array of the mouse click position relative to the map.
     */
    private int[] getMouseclickLocationRelativeToMap(MouseEvent e) {
        return new int[] { (e.getX() - this.gpanel.getWidth() / 2) + this.pos[0], e.getY() - (this.gpanel.getHeight() / 2) + this.pos[1] };
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
            appDataFolder = System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support";
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

    /**
     * Converts all player created objects into a {@code String} that can be send
     * over the server.
     * 
     * @param markDataAsSend - if {@code true}, data from the qued list will be
     *                       cleared.
     * @return - {@code String} data.
     */
    private String getPlayerObjectDataString(boolean markDataAsSend) {
        ArrayList<Bullet> bullets;

        synchronized (this.quedPlayerObjects) {
            if (markDataAsSend) {
                if (this.quedPlayerObjects instanceof ArrayList<?>) {
                    bullets = new ArrayList<>((ArrayList<Bullet>) this.quedPlayerObjects);
                    this.quedPlayerObjects.clear();
                } else {
                    LOGGER.addLog("Error while casting the qued bullet list. ");
                    this.quedPlayerObjects.clear();
                    bullets = new ArrayList<Bullet>();
                }
            } else {
                bullets = this.quedPlayerObjects;
            }
        }

        StringBuilder data = new StringBuilder();
        for (Bullet b : bullets) {
            data.append(b.toString()).append(",");
        }

        // Remove trailing comma
        if (data.length() > 0) {
            data.setLength(data.length() - 1);
        }

        return data.toString();
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
            gpanel.removeWidgetsOfClass(ConnectionWidget.class);
            if (movementEnabled) {
                move(pressedKeys);
            }
            if (pressedKeys.isEmpty()) {
                localPlayer.switchAnimation("idle");
            }
            updatePlayerData();
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
