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

package flaggiserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Iterator;

import flaggiserver.common.Bullet;
import flaggiserver.common.ClientStruct;
import flaggiserver.common.Logger;
import flaggiserver.common.Rectangle;
import flaggiserver.common.Logger.LogLevel;

/**
 * Server class for the LAN Game application.
 * 
 */
public class Server {

    /////////////////
    // Constants
    ////////////////

    private static final int TCP_PORT = 54321;
    private static final int UDP_PORT = 54322;
    private static final int CLIENT_TIMEOUT_SECONDS = 10;
    private static final String DATA_DIRECTORY_NAME = "kireiiiiiiii.flaggi-server";

    /////////////////
    // Variables
    ////////////////

    public static List<ClientStruct> clients;
    public static List<Bullet> playerObjects;

    private static int maxClientID = 0;
    private static List<Integer> deadClientIdQueue;
    private static GameLoop gameLoop;

    /////////////////
    // Main
    ////////////////

    /**
     * Main method.
     * 
     * @param args
     */
    public static void main(String[] args) {

        // ---- Initialize & log
        logServerCreation();
        clients = new CopyOnWriteArrayList<ClientStruct>();
        playerObjects = new CopyOnWriteArrayList<Bullet>();
        deadClientIdQueue = new CopyOnWriteArrayList<Integer>();
        gameLoop = new GameLoop(60);
        gameLoop.start();

        // ---- Start listener threads
        new Thread(Server::startTCPListener, "TCP listener").start();
        new Thread(Server::startUDPListener, "UDP listener").start();

    }

    /////////////////
    // TCP Listener
    ////////////////

    /**
     * Starts the TCP listener for new client connections. Assigns IDs, handles
     * requests, and provides UDP port information. Designed to run on a separate
     * thread.
     * 
     */
    private static void startTCPListener() {
        Logger.log(LogLevel.INFO, "TCP listener started on port '" + TCP_PORT + "'. Waiting for clients...");

        try (ServerSocket serverSocket = new ServerSocket(TCP_PORT)) {

            while (true) {
                try (Socket clientSocket = serverSocket.accept()) {
                    handleClientConnection(clientSocket);
                } catch (SocketTimeoutException e) {
                    Logger.log(LogLevel.WARN, "TCP connection timed out.", e);
                } catch (IOException e) {
                    Logger.log(LogLevel.ERROR, "IO exception occurred in TCP listener.", e);
                    handleFatalError();
                } catch (Exception e) {
                    Logger.log(LogLevel.ERROR, "Unexpected error in TCP listener.", e);
                    handleFatalError();
                }
            }

        } catch (IOException e) {
            Logger.log(LogLevel.ERROR, "IO Exception while creating the TCP socket.", e);
            handleFatalError();
        }
    }

    /**
     * Handles an individual client connection over TCP.
     * 
     */
    private static void handleClientConnection(Socket clientSocket) throws IOException {
        // Initialize I/O streams
        clientSocket.setSoTimeout(500);
        ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

        // Read the initial message
        String initialMessage = in.readUTF();

        if ("ping".equals(initialMessage)) {
            handlePingRequest(out);
        } else if (initialMessage.startsWith("get-idle-clients")) {
            handleIdleClientsRequest(out, initialMessage);
        } else if (initialMessage.startsWith("new-client:")) {
            handleNewClientRequest(out, clientSocket, initialMessage);
        } else {
            Logger.log(LogLevel.WARN, "Invalid TCP message received: '" + initialMessage + "'");
        }
    }

    /**
     * Handles a ping request from a client.
     * 
     * @param out - target output stream.
     * @throws IOException
     */
    private static void handlePingRequest(ObjectOutputStream out) throws IOException {
        out.writeUTF("pong");
        out.flush();
        Logger.log(LogLevel.PING, "Handled 'is server running' check. Responded 'pong'");
    }

    /**
     * Handles an 'get-idle-clients' request from a client. This is required for the
     * lobby menu to work.
     * 
     * @param out     - target output stream.
     * @param message - the initial message.
     * @throws IOException
     */
    private static void handleIdleClientsRequest(ObjectOutputStream out, String message) throws IOException {
        try {
            int clientId = Integer.parseInt(message.split(":")[1]);
            String clientsData = getPlayerNameData(clients, clientId);

            out.writeUTF(clientsData);
            out.flush();

            Logger.log(LogLevel.TCPREQUESTS, "Handled 'get-idle-clients' request.");
        } catch (NumberFormatException e) {
            Logger.log(LogLevel.WARN, "Invalid client ID in 'get-idle-clients' request: " + message, e);
        }
    }

    /**
     * Handles a new client connection to the server.
     * 
     * @param out          - target output stream.
     * @param clientSocket - target client socket.
     * @param message      - initial message.
     * @throws IOException
     */
    private static void handleNewClientRequest(ObjectOutputStream out, Socket clientSocket, String message) throws IOException {
        // Parse client name
        String clientName = message.split(":")[1];
        int clientId = maxClientID++;
        InetAddress clientAddress = clientSocket.getInetAddress();

        // Register the new client
        synchronized (clients) {
            clients.add(new ClientStruct(clientId, clientName, clientAddress));
        }

        Logger.log(LogLevel.CONNECTION, "Client '" + clientName + "' connected. Assigned ID: " + clientId);

        // Send client ID and UDP port
        out.writeInt(clientId);
        out.writeInt(UDP_PORT);
        out.flush();

        Logger.log(LogLevel.CONNECTION, "Sent UDP port and ID back to client '" + clientName + "'");
    }

    /////////////////
    // UDP Listener
    ////////////////

    /**
     * Starts the UDP listener for receiving client position data and handling
     * heartbeat messages. Usually meant to be run on a separate thread.
     * 
     */
    private static void startUDPListener() {
        Logger.log(LogLevel.INFO, "UDP listener started on port '" + UDP_PORT + "'. Waiting for data...");

        try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {
            udpSocket.setSoTimeout(CLIENT_TIMEOUT_SECONDS * 1000);

            byte[] buffer = new byte[1024];
            playerObjects = new ArrayList<>();
            deadClientIdQueue = new ArrayList<>();

            while (true) {

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    udpSocket.receive(packet);
                    processIncomingPacket(udpSocket, packet);
                } catch (SocketTimeoutException e) {
                }

                checkForDisconnectedClients();
                refreshIDNumberIfNoUsers();

                // Reset buffer for the next packet
                buffer = new byte[1024];
            }

        } catch (IOException e) {
            Logger.log(LogLevel.ERROR, "An IO Exception occurred in the UDP listener.", e);
            handleFatalError();
        }
    }

    /**
     * Processes an incoming UDP packet and handles client messages.
     * 
     */
    private static void processIncomingPacket(DatagramSocket udpSocket, DatagramPacket packet) throws IOException {
        String message = new String(packet.getData(), 0, packet.getLength());
        String[] parts = message.split(",");

        // Handle client disconnect
        if (isDisconnectMessage(parts)) {
            handleClientDisconnect(parts[0]);
            return;
        }

        // Validate packet structure
        if (parts.length < 6) {
            Logger.log(LogLevel.WARN, "Received malformed UDP message: " + Arrays.toString(parts));
            return;
        }

        // Parse client data
        int clientId = Integer.parseInt(parts[0]);
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int health = Integer.parseInt(parts[3]);
        // Username is on parts[4]
        String animationFrame = parts[5];
        String playerObjectData = (parts.length < 7) ? null : parts[6];

        ClientStruct client;
        synchronized (clients) {
            client = getClient(clientId);
        }

        if (client != null) {
            updateClientData(client, x, y, health, animationFrame, playerObjectData);

            String responseMessage = isClientRecentlyDead(clientId) ? "died" : getAllClientsData(clientId);
            sendResponse(udpSocket, client, responseMessage, packet.getPort());
        }
    }

    /**
     * Checks if a client UDP message is a disconnect message.
     * 
     * @param parts - split recieved message from client.
     * @return true if it is a disconnect message, false otherwise.
     */
    private static boolean isDisconnectMessage(String[] parts) {
        return parts.length >= 2 && "disconnect".equals(parts[1]);
    }

    /**
     * Handles player disconnects.
     * 
     * @param clientIdStr - client ID in a {@code String} form.
     */
    private static void handleClientDisconnect(String clientIdStr) {
        int clientId = Integer.parseInt(clientIdStr);
        synchronized (clients) { // Synchronize if needed
            for (ClientStruct client : clients) {
                if (client.getID() == clientId) {
                    Logger.log(LogLevel.CONNECTION, "Client '" + client.getDISPLAY_NAME() + "' disconnected.");
                    clients.remove(client); // Directly remove the client
                    break;
                }
            }
        }
    }

    /**
     * Updates the data of a client.
     * 
     * @param client           - target {@code Client} object.
     * @param x                - X position.
     * @param y                - Y position.
     * @param health           - client health
     * @param animationFrame   - animation daya.
     * @param playerObjectData - player objects.
     */
    private static void updateClientData(ClientStruct client, int x, int y, int health, String animationFrame, String playerObjectData) {
        client.setPosition(x, y);
        client.setAnimationFrame(animationFrame);
        client.updateLastReceivedTime();

        if (health == -1) {
            client.setHealth(100); // Reset health if required
        }

        if (playerObjectData != null) {
            handlePlayerObjectData(playerObjectData, client);
        }
    }

    /**
     * Adds all new bullets reported by the client.
     * 
     * @param dataString - recipe for the bullet.
     * @param client     - owning client.
     */
    private static void handlePlayerObjectData(String dataString, ClientStruct client) {
        Bullet b = dataToBullet(dataString, client.getID());
        synchronized (playerObjects) {
            playerObjects.add(b);
        }
        client.addPlayerObject(b);
    }

    /**
     * Checks if a client recently died.
     * 
     * @param clientId - target client ID.
     * @return true if the client is recently dead, false otherwise.
     */
    private static boolean isClientRecentlyDead(int clientId) {
        synchronized (deadClientIdQueue) {
            Iterator<Integer> iterator = deadClientIdQueue.iterator();
            while (iterator.hasNext()) {
                if (iterator.next() == clientId) {
                    iterator.remove();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sends a response of all player data back to the client.
     * 
     * @param udpSocket - target UDP socket.
     * @param client    - target client.
     * @param message   - response message/
     * @param port      - port number.
     * @throws IOException if an error occurs.
     */
    private static void sendResponse(DatagramSocket udpSocket, ClientStruct client, String message, int port) throws IOException {
        byte[] responseBuffer = message.getBytes();
        DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length, client.getINET_ADRESS(), port);
        udpSocket.send(responsePacket);
    }

    /////////////////
    // Helper methods
    ////////////////

    /**
     * Method handeling the occurance of a fatal unrecoverable error.
     * 
     */
    private static void handleFatalError() {
        Logger.log(LogLevel.ERROR, "FATAL EXCEPTION CAUGHT! SHUTTING DOWN SERVER...");
        System.exit(0);
    }

    /**
     * Logs the server creation message and the IP it was created on.
     * 
     */
    private static void logServerCreation() {
        Logger.setLogFile(getApplicationDataFolder() + File.separator + "logs" + File.separator + "latest.log");
        if (isRunningInDocker()) {
            String hostIp = getHostIP() == null ? "." : ": " + getHostIP();
            Logger.log(LogLevel.INFO, "Server is running in Docker. Use host's IP adress to connect" + hostIp);
        } else {
            Logger.log(LogLevel.INFO, "Server socket created on IP: '" + getLocalIPv4Address().getHostAddress() + "'");
        }
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
     * Gets the {@code String} of player object data to send to server.
     * 
     * @return - data.
     */
    private static String getAllPlayerObjectData(int id) {
        StringBuilder data = new StringBuilder();

        synchronized (playerObjects) {
            for (Bullet bullet : new ArrayList<>(playerObjects)) {
                if (!bullet.wasCreationDataSendToClient(id)) {
                    data.append(bullet.toString()).append("+");
                }
                bullet.setWasSendToClient(id);
            }

            // Remove trailing "+" if present
            if (data.length() > 0 && data.charAt(data.length() - 1) == '+') {
                data.setLength(data.length() - 1);
            }
        }

        data.append("/");

        synchronized (clients) {
            for (ClientStruct client : new ArrayList<>(clients)) {
                List<Bullet> playerObjects = new ArrayList<>(client.getPlayerObjects());
                for (Bullet object : playerObjects) {
                    data.append(client.getID()).append("-").append(object.getBulletId()).append(",");
                }
            }

            // Remove trailing "," if present
            if (data.length() > 0 && data.charAt(data.length() - 1) == ',') {
                data.setLength(data.length() - 1);
            }
        }

        return data.toString();
    }

    /**
     * Gets a data {@code String} of names of clients from input list, and separates
     * them by a comma.
     * 
     * @param clients - target client list.
     * @return client names separated by a comma.
     */
    private static String getPlayerNameData(List<ClientStruct> clients, int blacklist) {
        List<ClientStruct> tempClients = new ArrayList<ClientStruct>(clients);
        String clientNames = "";
        for (ClientStruct client : tempClients) {
            if (client.getID() != blacklist) {
                clientNames += client.getDISPLAY_NAME() + ",";
            }
        }
        return clientNames;
    }

    /**
     * Coverts bullet creation data into a bullet object
     * 
     * @param data
     * @return
     */
    private static Bullet dataToBullet(String data, int clientId) {
        String[] parsedData = data.split(":");
        if (parsedData.length != 6) {
            Logger.log(LogLevel.WARN, "Recieved invalid data format for creating a bullet object: " + Arrays.toString(parsedData));
            return null;
        }
        int bulletNum = Integer.parseInt(parsedData[1]);
        String[] initPosData = parsedData[2].split("&");
        String[] targetPosData = parsedData[3].split("&");
        int[] initPos = new int[] { Integer.parseInt(initPosData[0]), Integer.parseInt(initPosData[1]) };
        int[] targetPos = new int[] { Integer.parseInt(targetPosData[0]), Integer.parseInt(targetPosData[1]) };
        int decayTime = Integer.parseInt(parsedData[4]);
        int initVelocity = Integer.parseInt(parsedData[5]);

        return new Bullet(initPos, targetPos, initVelocity, decayTime, clientId, bulletNum);
    }

    /**
     * Checks, if any clients reached the timeout limit => disconnected. If so,
     * removes them.
     * 
     */
    private static void checkForDisconnectedClients() {
        synchronized (clients) {
            long currentTime = System.currentTimeMillis();
            Iterator<ClientStruct> iterator = clients.iterator();

            while (iterator.hasNext()) {
                ClientStruct client = iterator.next();
                long timeDifference = currentTime - client.getLastReceivedTime();

                if (timeDifference > CLIENT_TIMEOUT_SECONDS * 1000) {
                    Logger.log(LogLevel.WARN, "Client '" + client.getDISPLAY_NAME() + "' disconnected (Timed out!).");
                    clients.remove(client);
                }
            }
        }
    }

    /**
     * Checks if any users are connected, if not, resets the max client id back to 0
     * to avoid huge id numbers.
     * 
     */
    private static void refreshIDNumberIfNoUsers() {
        if (clients.isEmpty() && maxClientID > 0) {
            maxClientID = 0;
            Logger.log(LogLevel.INFO, "No users connected, resetting ID number to 0.");
        }
    }

    /**
     * Accesor for the client using the client ID.
     * 
     * @param id - client ID
     * @return a {@code Client} object reference. If not found, returns null.
     */
    private static ClientStruct getClient(int id) {
        synchronized (clients) {
            for (ClientStruct c : clients) {
                if (c.getID() == id) {
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Gets all positions of all connected clients.
     * 
     * @return - a {@code String} of all client positions.
     */
    private static String getAllClientsData(int id) {
        StringBuilder positions = new StringBuilder();
        synchronized (clients) {
            for (ClientStruct client : clients) {
                positions.append(client.getID()).append(",").append(client.getX()).append(",").append(client.getY()).append(",").append(client.getHealth()).append(",").append(client.getDISPLAY_NAME()).append(",").append(client.getAnimationFrame()).append(";");
            }
        }
        positions.append("|").append(getAllPlayerObjectData(id));
        return positions.toString();
    }

    /**
     * Gets the local IP adress.
     * 
     * @return - {@code InetAdress} of the local IP.
     */
    private static InetAddress getLocalIPv4Address() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress instanceof java.net.Inet4Address) {
                        return inetAddress;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the host's IP when running from a Docker container.
     * 
     * @return = - {@code String} of the host IP, null if not accesible.
     */
    private static String getHostIP() {
        String hostIP = System.getenv("HOST_IP");
        return hostIP.length() > 0 ? hostIP : null;
    }

    /**
     * Checks if a program is running in a Docker container or not.
     * 
     * @return - true if running in a Docker container, false otherwise.
     */
    private static boolean isRunningInDocker() {
        if (new File("/.dockerenv").exists()) {
            return true;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/1/cgroup"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("docker") || line.contains("containerd")) {
                    return true;
                }
            }
        } catch (IOException e) {
        }

        return false;
    }

    /**
     * Calculates and returns the hitbox for a given client.
     */
    private static Rectangle getPlayerHitbox(ClientStruct client) {
        final int OFFSET_X = 7; // Offset for the X-coordinate
        final int OFFSET_Y = 7; // Offset for the Y-coordinate
        final int WIDTH = 53; // Width of the player's hitbox
        final int HEIGHT = 93; // Height of the player's hitbox

        return new Rectangle(client.getX() + OFFSET_X, client.getY() + OFFSET_Y, WIDTH, HEIGHT);
    }

    /**
     * Handles the collision between a bullet and a player.
     */
    private static void handleBulletCollision(Bullet bullet, ClientStruct target, List<Bullet> bulletsToRemove) {
        // Remove the bullet from its owner's object list
        removeBulletFromOwner(bullet);

        // Mark bullet for removal
        bulletsToRemove.add(bullet);

        // Update player's health
        int newHealth = Math.max(target.getHealth() - 10, 0); // Prevent negative health
        target.setHealth(newHealth);

        // Mark player as dead if health is depleted
        if (newHealth == 0) {
            deadClientIdQueue.add(target.getID());
        }

        Logger.log(LogLevel.DEBUG, "Bullet hit player '" + target.getDISPLAY_NAME() + "'. Health: " + newHealth);
    }

    /**
     * Removes a bullet from its owner's list of player objects.
     */
    private static void removeBulletFromOwner(Bullet bullet) {
        for (ClientStruct owner : new ArrayList<>(clients)) { // Avoid concurrent modification
            if (owner.getID() == bullet.getOwningPlaterId()) {
                owner.removePlayerObject(bullet);
                break;
            }
        }
    }

    /**
     * Removes bullets that have collided with players.
     */
    private static void removeBullets(List<Bullet> bulletsToRemove) {
        try {
            playerObjects.removeAll(bulletsToRemove);
        } catch (IndexOutOfBoundsException e) {
            Logger.log(LogLevel.ERROR, "Failed to remove bullets during update.", e);
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
    private static class GameLoop implements Runnable {

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
            Logger.log(LogLevel.INFO, "Started game loop.");
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
         * Set a new FPS value.
         * 
         * @param value - new FPS value.
         */
        public void setFps(int value) {
            targetFPS = value;
        }

        /**
         * Updates the game state by processing collisions between bullets and players,
         * updating health, and marking dead clients.
         */
        private void update() {
            // Check if there are active players and bullets to process
            if (playerObjects == null || clients == null || playerObjects.isEmpty() || clients.isEmpty()) {
                return;
            }

            // Bullets to be removed after processing
            List<Bullet> bulletsToRemove = new ArrayList<>();

            // Process each bullet
            for (Bullet bullet : new ArrayList<>(playerObjects)) { // Avoid concurrent modification
                Rectangle bulletHitbox = bullet.getHitbox();

                // Check collision with each client
                for (ClientStruct client : new ArrayList<>(clients)) { // Avoid concurrent modification
                    Rectangle playerHitbox = getPlayerHitbox(client);

                    // Detect collision between bullet and player
                    if (bulletHitbox.intersects(playerHitbox) && bullet.getOwningPlaterId() != client.getID()) {
                        handleBulletCollision(bullet, client, bulletsToRemove);
                        break; // Exit loop since collision has been processed
                    }
                }
            }

            // Remove bullets that hit players
            removeBullets(bulletsToRemove);
        }

    }

}
