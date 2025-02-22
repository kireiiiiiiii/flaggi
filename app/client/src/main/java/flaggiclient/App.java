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

package flaggiclient;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import flaggiclient.common.AppOptions;
import flaggiclient.common.Client;
import flaggiclient.common.Client.ServerMessageHandeler;
import flaggiclient.common.Client.ServerRequests;
import flaggiclient.common.Client.ServerResponses;
import flaggiclient.common.Logger;
import flaggiclient.common.Sprite;
import flaggiclient.constants.WidgetTags;
import flaggiclient.struct.ClientStruct;
import flaggiclient.struct.GameDataStruct;
import flaggiclient.ui.Background;
import flaggiclient.ui.Bullet;
import flaggiclient.ui.ConfirmationWindow;
import flaggiclient.ui.ConnectionWidget;
import flaggiclient.ui.Flag;
import flaggiclient.ui.Floor;
import flaggiclient.ui.HUD;
import flaggiclient.ui.InviteScreen;
import flaggiclient.ui.InviteScreen.LobbyHandler;
import flaggiclient.ui.MenuScreen;
import flaggiclient.ui.PauseMenu;
import flaggiclient.ui.Player;
import flaggiclient.ui.ToastManager;
import flaggiclient.ui.Tree;
import flaggishared.common.GPanel;
import flaggishared.common.GPanel.Interactable;
import flaggishared.common.GPanel.InteractableHandeler;
import flaggishared.common.GPanel.Renderable;
import flaggishared.common.GPanel.Scrollable;
import flaggishared.common.GPanel.Typable;
import flaggishared.common.MapData;
import flaggishared.common.MapData.ObjectData;
import flaggishared.util.ImageUtil;
import flaggishared.util.ScreenUtil;
import flaggishared.common.PersistentValue;

/**
 * Main class for the LAN Game application.
 */
public class App implements InteractableHandeler, LobbyHandler, ServerMessageHandeler {

    /////////////////
    // Constants
    ////////////////

    public static final String PROJECT_NAME = "Flaggi";
    public static final String DATA_DIRECTORY_NAME = "kireiiiiiiii.flaggi";
    public static final String FILE_JAR_SEPARATOR = "/";
    public static final Logger LOGGER = Logger.getLogger(getApplicationDataFolder() + File.separator + "logs" + File.separator + "app.log"); // TODO Fix lobby
    public static final int TCP_PORT = 54321;
    public static final int FPS = 60;
    public static final boolean SHOW_HITBOXES = false;

    /////////////////
    // Variables
    ////////////////

    private Client localClient;
    private Player localPlayer;
    private String username, serverIP;
    private int clientID, health, speed;
    private GPanel gpanel;
    private GameLoop gameLoop;
    private PersistentValue<AppOptions> appOptions;
    private ArrayList<KeyEvent> pressedKeys;
    private ArrayList<Bullet> quedPlayerObjects;
    private ToastManager toasts;
    private ConfirmationWindow yesnoToasts;
    private int[] pos, windowSize;
    private boolean movementEnabled, paused;
    private MapData currentMap;

    /////////////////
    // Main & Constructor
    ////////////////

    /**
     * Main method.
     *
     * @param args
     */
    public static void main(String[] args) {
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

        // ------ Get user settings
        try {
            this.appOptions = new PersistentValue<AppOptions>(getApplicationDataFolder() + File.separator + "user-options.json", AppOptions.class);
        } catch (IOException e) {
            LOGGER.addLog("Error loading app options.", e);
            new File(getApplicationDataFolder() + File.separator + "user-options.json").delete();
            this.appOptions = new PersistentValue<AppOptions>(getApplicationDataFolder() + File.separator + "user-options.json");
            this.appOptions.set(getDefaultOptions());
            try {
                this.appOptions.save();
            } catch (IOException e1) {
            }
        }

        if (this.appOptions.get() == null) {
            this.appOptions.set(getDefaultOptions());
        }

        // ------ Initialize
        this.windowSize = ScreenUtil.getScreenDimensions();
        this.pos = new int[2];
        this.pos[0] = 0;
        this.pos[1] = 0;
        this.speed = 10;
        this.movementEnabled = false;
        this.paused = false;
        this.pressedKeys = new ArrayList<KeyEvent>();
        this.quedPlayerObjects = new ArrayList<Bullet>();
        this.toasts = new ToastManager();
        this.yesnoToasts = new ConfirmationWindow();
        printHeader();

        // ------ Initialize UI
        this.gpanel = new GPanel(this, FPS, this.windowSize[0], this.windowSize[1], false, PROJECT_NAME, new Color(229, 204, 255));
        try {
            Image iconWin = ImageUtil.getImageFromFile("icons/icon_win.png");
            Image iconMac = ImageUtil.getImageFromFile("icons/icon_mac.png");
            this.gpanel.setIconOSDependend(iconWin, iconMac, iconWin, iconWin);
        } catch (IOException e) {
            LOGGER.addLog("Error loading app icons. Icon files do not exist!", e);
        }
        this.gpanel.setExitOperation(() -> {
            exitServer();
        });
        initializeWidgets();
        this.gpanel.add(this.toasts);
        this.gpanel.add(this.yesnoToasts);
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
    public void joinServer() {

        // ---- Set variables
        this.health = -1;

        // ------ Get entered username
        for (MenuScreen m : this.gpanel.getWidgetsByClass(MenuScreen.class)) {
            this.username = m.getEnteredUsername();
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
        this.serverIP = "";
        for (MenuScreen m : this.gpanel.getWidgetsByClass(MenuScreen.class)) {
            serverIP = m.getEnteredIP();
        }
        if (serverIP.length() < 1) {
            for (MenuScreen m : this.gpanel.getWidgetsByClass(MenuScreen.class)) {
                m.setErrorMessage("No server IP entered.");
            }
            return;
        }

        InetAddress serverAddress;
        try {
            serverAddress = InetAddress.getByName(serverIP);
        } catch (UnknownHostException e) {
            LOGGER.addLog("Unknown host exception.", e);
            return;
        }
        LOGGER.addLog("Selected ip: " + serverIP);

        // ------ Check if a Flaggi server is reachable there
        if (!Client.isFlaggiServerRunning(serverAddress, TCP_PORT)) {
            for (MenuScreen m : this.gpanel.getWidgetsByClass(MenuScreen.class)) {
                m.setErrorMessage("Not a valid IP. Try again.");
                return;
            }
        }

        // ------ Set the skin name
        String skinName = Player.DEFAULT_SKIN; // default skin
        if (username.equals("daarlin") || username.equals("owo")) {
            skinName = "venom";
        } else if (username.equals("snapshot")) {
            skinName = "jester";
        }

        // ------ Initialize client & change UI
        this.localClient = new Client(username, serverAddress, this);
        this.clientID = this.localClient.getId();
        this.localPlayer = new Player(new int[] { ScreenUtil.getScreenDimensions()[0] / 2, ScreenUtil.getScreenDimensions()[1] / 2 }, username, skinName, this.clientID);
        this.gpanel.add(this.localPlayer);
        this.gpanel.hideAllWidgets();
        this.gpanel.add(new InviteScreen(this, () -> {
            this.localClient.sendTCPMessageToServer(ServerRequests.GET_IDLE_CLIENTS);
        }));
        this.gpanel.showTaggedWidgets(WidgetTags.LOBBY);

        // ------ Start game loop
        this.movementEnabled = true;
        this.gameLoop = new GameLoop(FPS);
        this.gameLoop.start();
        LOGGER.addLog("Game loop started");
    }

    /**
     * Enters a match.
     *
     */
    public void enterGame(String message) {
        String[] parts = message.split("/");
        String jsonMapData = parts[1];
        boolean first = parts[2].equals("true");
        this.currentMap = PersistentValue.fromJson(jsonMapData, MapData.class).scaleMap(Sprite.SPRITE_SCALING);

        // ---- Remove old widgets
        for (Floor f : this.gpanel.getWidgetsByClass(Floor.class)) {
            this.gpanel.remove(f);
        }
        for (Tree t : this.gpanel.getWidgetsByClass(Tree.class)) {
            this.gpanel.remove(t);
        }
        for (Flag f : this.gpanel.getWidgetsByClass(Flag.class)) {
            this.gpanel.remove(f);
        }

        // ---- Add new widgets
        this.gpanel.add(new Floor(new int[] { this.currentMap.getWidth(), this.currentMap.getHeight() }));
        this.gpanel.add(gameObjectDataToUI(this.currentMap.getGameObjects(), first));

        // ---- Set the local player's position
        this.pos[0] = first ? this.currentMap.getSpawnpoint().oneX : this.currentMap.getSpawnpoint().twoX;
        this.pos[1] = first ? this.currentMap.getSpawnpoint().oneY : this.currentMap.getSpawnpoint().twoY;
        this.localPlayer.setPos(new int[] { ScreenUtil.getScreenDimensions()[0] / 2, ScreenUtil.getScreenDimensions()[1] / 2 });

        // ---- Set render
        this.movementEnabled = true;
        this.localPlayer.setHealth(-1);
        this.gpanel.hideAllWidgets();
        this.gpanel.showTaggedWidgets(WidgetTags.GAME_ELEMENTS);
        updateCameraPosition();
    }

    public void goIdle() {
        this.movementEnabled = false;
        this.gpanel.hideAllWidgets();
        this.gpanel.showTaggedWidgets(WidgetTags.LOBBY); // TODO resume update task
                                                         // TODO stop game loop?
    }

    /**
     * Event done on exit the game.
     *
     */
    public void exitServer() {
        if (this.gameLoop != null)
            this.gameLoop.stop();
        if (this.localClient != null)
            this.localClient.disconnectFromServer();
        try {
            if (this.username != null && this.serverIP != null) {
                this.appOptions.set(new AppOptions(this.username, this.serverIP));
            }
            this.appOptions.save();
            LOGGER.addLog("Menu fields data saved succesfully.");
        } catch (IOException e) {
            LOGGER.addLog("Saving menu fields data not succesful.", e);
        }
        System.exit(0);
    }

    /**
     * Method executed when the server times out. Goes back to the menu, and informs
     * the user about it/
     *
     */
    @Override
    public void timeout() {
        goToMenu();
        for (MenuScreen m : this.gpanel.getWidgetsByClass(MenuScreen.class)) {
            m.setErrorMessage("Server shut down. Please try again.");
        }
    }

    @Override
    public void handleMessage(String message) {
        if (ServerResponses.isLobbyList(message) != null) {
            String data = ServerResponses.isLobbyList(message);
            updateLobbyList(data);
        } else if (ServerResponses.isDied(message)) {
            int[] pos = new int[2];
            pos[0] = Integer.parseInt(message.split(":")[1].split(",")[0]);
            pos[1] = Integer.parseInt(message.split(":")[1].split(",")[1]);
            die(pos);
        } else if (ServerResponses.isEnterGame(message)) {
            System.out.println("Enter game: " + message);
            enterGame(message);
        } else if (message.equals(ServerResponses.GO_IDLE)) {
            goIdle();
        } else if (message.equals(ServerResponses.FLAG_GRAB)) {
            localPlayer.hasFlag(true);
        } else {
            LOGGER.addLog("Received invalid message from server: " + message);
        }
    }

    @Override
    public void invitePlayer(String playerName, int playerID) {
        this.localClient.sendTCPMessageToServer(ServerRequests.invitePlayer(playerID));
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
        if (this.localClient != null)
            this.localClient.disconnectFromServer();
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
        LOGGER.addLog("Menu screen active.");
    }

    /**
     * Handles a player invite.
     *
     * @param playerName - player display name.
     * @param playerID   - player ID
     */
    public void handleInvite(String playerName, int playerID) {
        this.yesnoToasts.show("Accept invite from: " + playerName + "?", () -> {
            this.localClient.sendTCPMessageToServer(ServerRequests.acceptInvite(playerID));
        }, () -> {
            this.localClient.sendTCPMessageToServer(ServerRequests.declineInvite(playerID));
        });
    }

    /**
     * Moves the player based on the given speed and input events.
     *
     * @param inputEvents - {@code List<KeyEvent>} of the pressed keys.
     * @param speed       - The speed value (int) to scale movement.
     */
    public void move(List<KeyEvent> inputEvents) {
        boolean moveUp = false, moveDown = false, moveLeft = false, moveRight = false;
        List<KeyEvent> events = new ArrayList<>(inputEvents);

        for (KeyEvent e : events) {
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

        // Switch correct walking animations
        if ((moveUp && moveLeft) || (moveUp && moveRight)) { // TODO diagdown
            this.localPlayer.switchAnimation("walk_diagup");
        } else if ((moveDown && moveLeft) || (moveDown && moveRight)) {
            this.localPlayer.switchAnimation("idle");
        } else if (moveUp) {
            this.localPlayer.switchAnimation("walk_up");
        } else if (moveDown) {
            this.localPlayer.switchAnimation("walk_down");
        } else if (moveLeft) {
            this.localPlayer.switchAnimation("walk_side");
        } else if (moveRight) {
            this.localPlayer.switchAnimation("walk_side");
        }

        // Calculate horizontal and vertical movement directions
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

        // Normalize the movement direction
        double magnitude = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (magnitude > 0) {
            deltaX = (deltaX / magnitude) * this.speed;
            deltaY = (deltaY / magnitude) * this.speed;
        }

        double tempX = this.pos[0] + deltaX;
        double tempY = this.pos[1] + deltaY;

        if (this.currentMap == null) {
            return;
        }

        // Validate new pos
        if (tempX < 0 || tempX > this.currentMap.getWidth() - Player.TEXTURE_WIDTH) {
            deltaX = 0;
        }
        if (tempY < 0 || tempY > this.currentMap.getHeight() - Player.TEXTURE_HEIGHT) {
            deltaY = 0;
        }

        // Update the player's position
        this.pos[0] += deltaX;
        this.pos[1] += deltaY;

        // Update the viewport
        updateCameraPosition();
    }

    /**
     * Shoots a bullet from the player.
     *
     * @param e - {@code MouseEvent} of the mouse click.
     */
    public void shoot(MouseEvent e) {
        Bullet b = new Bullet(this.pos, getMouseclickLocationRelativeToGpanel(e, this.gpanel, this.pos), 1000 + this.speed * 10, 2000, this.clientID);
        Runnable afterDecay = () -> {
            this.gpanel.remove(b);
        };
        b.setAfterDecayRunnable(afterDecay);
        this.quedPlayerObjects.add(b);
    }

    /**
     * Updates the lobby list.
     *
     * @param list - target list.
     */
    public void updateLobbyList(String list) {
        Map<Integer, String> clients = new HashMap<>();

        if (!list.equals("")) {
            String[] displayNames = list.split(",");
            for (String client : displayNames) {
                clients.put(Integer.parseInt(client.substring(0, 1)), client.substring(1));
            }
        }

        for (InviteScreen l : this.gpanel.getWidgetsByClass(InviteScreen.class)) {
            l.setClients(clients);
        }
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

        // Get the current players from the panel and their positions from the
        // server
        ArrayList<Player> players = this.gpanel.getWidgetsByClass(Player.class);
        GameDataStruct struct = localClient.updatePlayerPositions(new ClientStruct(pos[0], pos[1], this.clientID, this.health, this.username, localAnimationFrame, getPlayerObjectDataString(true)));
        if (struct == null) {
            return;
        }

        List<ClientStruct> serverPositions = struct.connectedClientsList;
        String playerObjectData = struct.playerObjectData;

        // Get the local player, and remove it from the rendering list
        ClientStruct localPlayerStruct = null;
        for (ClientStruct cs : serverPositions) { // Find the local player
            if (cs.getId() == this.clientID) {
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
            if (remainingPlayer.getId() == this.clientID) {
                continue;
            }
            this.gpanel.remove(remainingPlayer);
        }
    }

    /**
     * Method executed when player dies.
     *
     */
    public void die(int spawnPoint[]) {
        this.health = -1;
        this.pos[0] = spawnPoint[0] * Sprite.SPRITE_SCALING;
        this.pos[1] = spawnPoint[1] * Sprite.SPRITE_SCALING;
    }

    /**
     * Initializes all new widgets and adds them to the gpanel.
     *
     */
    public void initializeWidgets() {
        ArrayList<Renderable> widgets = new ArrayList<Renderable>(Arrays.asList(new Background(), new MenuScreen(() -> {
            joinServer();
        }, this.appOptions.get().name, this.appOptions.get().ip), new PauseMenu(() -> {
            togglePauseMenu();
        }, () -> {
            goToMenu();
        }), new HUD()));

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
    public void mouseWheelMoved(MouseWheelEvent e) {
        for (Renderable r : this.gpanel.getScrollables()) {
            Scrollable s = (Scrollable) r;
            s.scroll(e);
        }
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
    private static void printHeader() {
        System.out.println("\n" + " ______   __         ______     ______     ______     __    \n" + "/\\  ___\\ /\\ \\       /\\  __ \\   /\\  ___\\   /\\  ___\\   " + "/\\ \\   \n" + "\\ \\  __\\ \\ \\ \\____  \\ \\  __ \\  \\ \\ \\__ \\  \\ \\ \\__ " + "\\  \\ \\ \\  \n" + " \\ \\_\\    \\ \\_____\\  \\ \\_\\ \\_\\  \\ \\_____\\  \\ " + "\\_____\\  \\ \\_\\ \n" + "  \\/_/     \\/_____/   \\/_/\\/_/   \\/_____/   \\/_____/   \\/_/ \n" + "                                                            \n" + "");
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
    private static int[] getMouseclickLocationRelativeToGpanel(MouseEvent e, GPanel gpanel, int[] pos) {
        return new int[] { (e.getX() - gpanel.getWidth() / 2) + pos[0], e.getY() - (gpanel.getHeight() / 2) + pos[1] };
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
    private static AppOptions getDefaultOptions() {
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

    /**
     * Converts the game object data into UI objects.
     *
     * @param objectData - {@code List<ObjectData>} of the game objects.
     * @return
     */
    private static List<Renderable> gameObjectDataToUI(List<ObjectData> objectData, boolean blue) {
        List<Renderable> objects = new ArrayList<>();
        for (ObjectData data : objectData) {
            int[] pos = new int[] { data.getX(), data.getY() };
            // ObjectType.
            switch (data.getObjectType()) {
            case TREE:
                objects.add(new Tree(pos));
                break;
            case BLUE_FLAG:
                objects.add(new Flag(pos, !blue));
                break;
            case RED_FLAG:
                objects.add(new Flag(pos, blue));
                break;
            default:
                break;
            }
        }
        return objects;
    }

    /**
     * Updates the camera position.
     */
    private void updateCameraPosition() {
        int[] screen = ScreenUtil.getScreenDimensions();
        this.gpanel.setCameraPosition(new int[] { -this.pos[0] + screen[0] / 2, -this.pos[1] + screen[1] / 2 });
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
