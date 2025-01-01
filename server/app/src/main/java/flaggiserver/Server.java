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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Iterator;

import flaggiserver.common.Rectangle;

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
    @SuppressWarnings("unused")
    private static String DATA_DIRECTORY_NAME = "kireiiiiiiii.flaggi-server";

    /////////////////
    // Variables
    ////////////////

    private static int maxClientID = 0;
    private static List<Client> clients;
    private static List<Bullet> playerObjects;
    private static List<Integer> deadClientIdQue;
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
        try {
            startServer();
        } catch (IOException e) {
            log(RED, "Error starting server: " + e.getMessage());
        }
    }

    /////////////////
    // Events
    ////////////////

    /**
     * Starts the server by firts creating a socket, then the TCP port and then the
     * UDP port.
     * 
     * @throws IOException
     */
    public static void startServer() throws IOException {

        // ---- INITIALIZE
        clients = new CopyOnWriteArrayList<Client>();
        playerObjects = new CopyOnWriteArrayList<Bullet>();
        deadClientIdQue = new CopyOnWriteArrayList<Integer>();

        // ---- START LISTENERS & GAME LOOP
        ServerSocket serverSocket = new ServerSocket(TCP_PORT);
        if (isRunningInDocker()) {
            String hostIp = getHostIP() == null ? "." : ": " + getHostIP();
            log(YELLOW, "Server is running in Docker. Use host's IP adress to connect" + hostIp);
        } else {
            log(YELLOW, "Server socket created on IP: '" + getIPv4Address().getHostAddress() + "'");
        }

        new Thread(() -> startTCPListener(serverSocket)).start();
        log(YELLOW, "Server started on port '" + TCP_PORT + "'. Waiting for clients...");
        new Thread(Server::startUDPListener).start();
        log(YELLOW, "UDP started on port '" + UDP_PORT + "'. Waiting for data...");
        gameLoop = new GameLoop(60);
        gameLoop.start();
        log(YELLOW, "Started game loop.");
    }

    /**
     * Starts the TCP listener that listens for new clients, assigns them their IDs,
     * and gives them the UDP port to send data to.
     * 
     * @param serverSocket - TCP port
     */
    private static void startTCPListener(ServerSocket serverSocket) {

        while (true) {

            try (Socket clientSocket = serverSocket.accept()) {

                // ---- Initialize

                clientSocket.setSoTimeout(500);
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                String initialMessage = in.readUTF();

                // ---- Running check

                if ("ping".equals(initialMessage)) {
                    out.writeUTF("pong");
                    out.flush();
                    log(BLACK, "Handled 'is server running' check from client, responded 'pong'");
                }

                // ---- Lobby list request

                else if (initialMessage.startsWith("get-idle-clients")) {
                    int id = Integer.parseInt(initialMessage.split(":")[1]);
                    String clientsData = getPlayerNameData(clients, id);
                    out.writeUTF(clientsData);
                    out.flush();
                    log(BLACK, "Handeled get-idle-clients request from client.");
                }

                // ---- New client

                else if (initialMessage.startsWith("new-client:")) {
                    int clientId = maxClientID++;
                    String clientName = initialMessage.split(":")[1];
                    InetAddress clientAddress = clientSocket.getInetAddress();

                    synchronized (clients) {
                        clients.add(new Client(clientId, clientName, clientAddress));
                    }
                    log(GREEN, "Assigned ID '" + clientId + "' to client '" + clientName + "'");

                    out.writeInt(clientId);
                    out.writeInt(UDP_PORT);
                    out.flush();
                    log(PURPLE, "Sent port and ID to client '" + clientName + "'");
                }

                // ---- Invalid message

                else {
                    log(RED, "Invalid message from client: " + initialMessage);
                }

            } catch (SocketTimeoutException e) {
                log(RED, "TCP socket timed out: " + e.getMessage());
            } catch (IOException e) {
                log(RED, "Error handling client connection: " + e.getMessage());
            } catch (Exception e) {
                log(RED, "An unexpected error occurred: " + e.getMessage());
            }

        }

    }

    /**
     * Starts the UDP listener for the clients to send their position data to. Now
     * also responds to heartbeat messages from clients to confirm connectivity.
     */
    private static void startUDPListener() {
        try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {

            byte[] buffer = new byte[1024];
            udpSocket.setSoTimeout(CLIENT_TIMEOUT_SECONDS * 1000);

            // ---- Variables
            playerObjects = new ArrayList<>();
            deadClientIdQue = new ArrayList<>();

            while (true) {
                boolean timedOut = false;
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                try {
                    udpSocket.receive(packet);
                } catch (SocketTimeoutException e) {
                    timedOut = true;
                }

                if (!timedOut) {
                    String message = new String(packet.getData(), 0, packet.getLength());
                    String[] parts = message.split(",");
                    if (parts[1].equals("disconnect")) {
                        synchronized (clients) {
                            for (Client client : clients) {
                                if (client.getId() == Integer.parseInt(parts[0])) {
                                    log(CYAN, "Client '" + client.getDisplayName() + "' disconnected.");
                                    clients.remove(client);
                                    break;
                                }
                            }
                        }
                        continue;
                    }
                    if (parts.length >= 6) {
                        int clientId = Integer.parseInt(parts[0]);
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int health = Integer.parseInt(parts[3]);
                        // Username is on parts[4]
                        String animationFrame = parts[5];
                        String playerObjectData = (parts.length < 7) ? null : parts[6];

                        Client client;
                        synchronized (clients) {
                            client = getClient(clientId);
                        }

                        if (client != null) {

                            // ---- Client died handeling
                            boolean isDead = false;
                            Iterator<Integer> iterator = deadClientIdQue.iterator();
                            while (iterator.hasNext()) {
                                Integer id = iterator.next();
                                if (id == clientId) {
                                    isDead = true;
                                    iterator.remove();
                                }
                            }

                            // ---- Normal message handeling
                            client.setPosition(x, y);
                            client.setAnimationFrame(animationFrame);
                            client.updateLastReceivedTime();
                            if (health == -1) {
                                client.setHealth(100);
                            }
                            if (playerObjectData != null) {
                                handlePlayerObjectData(playerObjectData, client);
                            }

                            // ---- Get message to send

                            String sendMessage;
                            if (isDead) {
                                sendMessage = "died";
                            } else {
                                sendMessage = getAllClientsData(clientId);
                            }
                            byte[] responseBuffer = sendMessage.getBytes();
                            DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length, client.getInetAddress(), packet.getPort());

                            udpSocket.send(responsePacket);

                        }

                    } else {
                        log(RED, "Received message doesn't have at least 6 parts: " + Arrays.toString(parts));
                    }
                }

                checkForDisconnectedClients();
                refreshIDNumberIfNoUsers();

                buffer = new byte[1024];
            }
        } catch (IOException e) {
            log(RED, "Error in UDP listener: " + e.getMessage());
            System.exit(0);
        }
    }

    /////////////////
    // Helper methods
    ////////////////

    /**
     * Gets the {@code String} of player object data to send to server.
     * 
     * @return - data.
     */
    private static String getAllPlayerObjectData(int id) {
        StringBuilder data = new StringBuilder();

        synchronized (playerObjects) {
            for (Bullet bullet : new ArrayList<>(playerObjects)) {
                if (!bullet.wasSendTo(id)) {
                    data.append(bullet.toString()).append("+");
                }
                bullet.addId(id);
            }

            // Remove trailing "+" if present
            if (data.length() > 0 && data.charAt(data.length() - 1) == '+') {
                data.setLength(data.length() - 1);
            }
        }

        data.append("/");

        synchronized (clients) {
            for (Client client : new ArrayList<>(clients)) {
                List<Bullet> playerObjects = new ArrayList<>(client.getPlayerObjects());
                for (Bullet object : playerObjects) {
                    data.append(client.id).append("-").append(object.getBulletId()).append(",");
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
    private static String getPlayerNameData(List<Client> clients, int blacklist) {
        List<Client> tempClients = new ArrayList<Client>(clients);
        String clientNames = "";
        for (Client client : tempClients) {
            if (client.getId() != blacklist) {
                clientNames += client.getDisplayName() + ",";
            }
        }
        return clientNames;
    }

    /**
     * Adds all new bullets reported by the client.
     * 
     * @param dataString - recipe for the bullet.
     * @param client     - owning client.
     */
    private static void handlePlayerObjectData(String dataString, Client client) {
        Bullet b = dataToBullet(dataString, client.getId());
        synchronized (playerObjects) {
            playerObjects.add(b);
        }
        client.addPlayerObject(b);
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
            log(RED, "Invalid data format for creating a bullet object: " + Arrays.toString(parsedData));
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
            Iterator<Client> iterator = clients.iterator();

            while (iterator.hasNext()) { // TODO BUG
                Client client = iterator.next();
                long timeDifference = currentTime - client.getLastReceivedTime();

                if (timeDifference > CLIENT_TIMEOUT_SECONDS * 1000) {
                    log(RED, "Client '" + client.getDisplayName() + "' disconnected (Timed out).");
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
            log(CYAN, "No users connected, resetting ID number to 0.");
        }
    }

    /**
     * Accesor for the client using the client ID.
     * 
     * @param id - client ID
     * @return a {@code Client} object reference. If not found, returns null.
     */
    private static Client getClient(int id) {
        synchronized (clients) {
            for (Client c : clients) {
                if (c.getId() == id) {
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
            for (Client client : clients) {
                positions.append(client.getId()).append(",").append(client.getX()).append(",").append(client.getY()).append(",").append(client.getHealth()).append(",").append(client.getDisplayName()).append(",").append(client.getAnimationFrame()).append(";");
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
    private static InetAddress getIPv4Address() {
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
    public static String getHostIP() {
        String hostIP = System.getenv("HOST_IP");
        return hostIP.length() > 0 ? hostIP : null;
    }

    /**
     * Checks if a program is running in a Docker container or not.
     * 
     * @return - true if running in a Docker container, false otherwise.
     */
    public static boolean isRunningInDocker() {
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
     * Term colors.
     * 
     */
    public static final String BLACK = "\033[0;30m"; // ping
    public static final String RED = "\033[0;31m"; // err
    public static final String GREEN = "\033[0;32m"; // assigned
    public static final String YELLOW = "\033[0;33m"; // init server
    public static final String BLUE = "\033[0;34m"; // recieve
    public static final String PURPLE = "\033[0;35m"; // send
    public static final String CYAN = "\033[0;36m"; // disconnects, reset id
    public static final String WHITE = "\033[0;37m";

    /**
     * Logs messages into the console.
     * 
     * @param color   - color of the log. Empty {@code String} if no color needed.
     * @param message - message to log.
     */
    private static void log(String color, String message) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        System.out.println(LocalDateTime.now().format(timeFormatter) + " " + color + message + "\u001B[0m");
    }

    /////////////////
    // Client struct
    ////////////////

    /**
     * Structure for a client object.
     * 
     */
    private static class Client {

        private final int id;
        private final String displayName;
        private final InetAddress inetAddress;
        private String animationFrame;
        private int x, y, health;
        private long lastReceivedTime;
        private List<Bullet> playerObjects;

        public Client(int id, String displayName, InetAddress inetAddress) {
            this.playerObjects = new ArrayList<Bullet>();
            this.id = id;
            this.displayName = displayName;
            this.inetAddress = inetAddress;
            updateLastReceivedTime();
        }

        public int getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public InetAddress getInetAddress() {
            return inetAddress;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getHealth() {
            return this.health;
        }

        public void setHealth(int health) {
            this.health = health;
        }

        public String getAnimationFrame() {
            return this.animationFrame;
        }

        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void setAnimationFrame(String animationFrame) {
            this.animationFrame = animationFrame;
        }

        public long getLastReceivedTime() {
            return lastReceivedTime;
        }

        public void updateLastReceivedTime() {
            this.lastReceivedTime = System.currentTimeMillis();
        }

        public List<Bullet> getPlayerObjects() {
            return this.playerObjects;
        }

        public void addPlayerObject(Bullet bullet) {
            this.playerObjects.add(bullet);
        }

        public void removePlayerObject(Bullet bullet) {
            this.playerObjects.remove(bullet);
        }

    }

    /**
     * Bullet class, as an player created object.
     * 
     */
    public static class Bullet implements Runnable {

        // Init variables
        private int bulletNum, playerId, decayTime;
        private int[] initPos, targetPos;
        private List<Integer> sendCliens;

        // Other variables
        private double[] position, direction;
        private int velocity;
        private boolean running;
        private Thread decayUpdateThread;
        private Runnable afterDecay;

        /**
         * Bullet constructor.
         *
         * @param initialPosition - Initial position of the bullet [x, y].
         * @param targetPosition  - Target position the bullet heads to [x, y].
         * @param velocity        - Velocity in points per second.
         * @param decayTime       - Time (in ms) after which the bullet disappears.
         */
        public Bullet(int[] initialPosition, int[] targetPosition, int velocity, int decayTime, int playerId, int bulletNum) {
            this.position = new double[] { initialPosition[0], initialPosition[1] };

            this.afterDecay = () -> {
                synchronized (clients) {
                    Iterator<Client> iterator = clients.iterator();
                    while (iterator.hasNext()) {
                        Client c = iterator.next();
                        if (c.getId() == this.playerId) {
                            c.removePlayerObject(this);
                            break;
                        }
                    }
                }
                playerObjects.remove(this);
            };

            this.playerId = playerId;
            this.bulletNum = bulletNum;
            this.initPos = initialPosition;
            this.targetPos = targetPosition;
            this.velocity = velocity;
            this.decayTime = decayTime;
            this.running = true;

            this.sendCliens = new ArrayList<Integer>();

            // Calculate normalized direction vector
            double dx = targetPosition[0] - initialPosition[0];
            double dy = targetPosition[1] - initialPosition[1];
            double magnitude = Math.sqrt(dx * dx + dy * dy);
            this.direction = new double[] { dx / magnitude, dy / magnitude };

            // Start the movement thread
            this.decayUpdateThread = new Thread(this, "Bullet decay update thread");
            this.decayUpdateThread.start();
        }

        /////////////////
        // Helpers
        ////////////////

        public int getBulletId() {
            return bulletNum;
        }

        public void addId(int id) {
            this.sendCliens.add(id);
        }

        public boolean wasSendTo(int id) {
            return this.sendCliens.contains(id);
        }

        public int getOwningPlaterId() {
            return this.playerId;
        }

        @Override
        public String toString() {
            return "bullet:" + playerId + "-" + bulletNum + ":" + initPos[0] + "&" + initPos[1] + ":" + targetPos[0] + "&" + targetPos[1] + ":" + decayTime + ":" + velocity;
        }

        /////////////////
        // Movement Logic
        ////////////////

        /**
         * Thread for updating the bullet's position and handling its lifecycle.
         */
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            long lastUpdate = System.currentTimeMillis();

            while (running) {
                long currentTime = System.currentTimeMillis();
                long elapsedTime = currentTime - lastUpdate;

                // Update position based on velocity and elapsed time
                if (elapsedTime > 0) {
                    double delta = (elapsedTime / 1000.0) * this.velocity; // Distance to move in this time slice
                    this.position[0] += this.direction[0] * delta;
                    this.position[1] += this.direction[1] * delta;
                    lastUpdate = currentTime;
                }

                // Check for decay
                if (currentTime - startTime >= this.decayTime) {
                    this.afterDecay.run();
                    this.running = false;
                }

                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Stops the bullet thread.
         */
        public void stop() {
            this.running = false;
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
         * Update method that will pull player data from the server and display it to
         * the user.
         * 
         */
        private void update() {
            if (playerObjects != null && clients != null && !playerObjects.isEmpty() && !clients.isEmpty()) {
                List<Bullet> bulletsToRemove = new ArrayList<>();

                for (Bullet b : new ArrayList<>(playerObjects)) {
                    Rectangle bulletHitbox = new Rectangle((int) b.position[0], (int) b.position[1], 5, 5);

                    for (Client c : new ArrayList<>(clients)) {
                        Rectangle playerHitbox = new Rectangle(c.getX() + 7, c.getY() + 7, 53, 93);

                        if (bulletHitbox.intersects(playerHitbox)) {
                            if (b.getOwningPlaterId() != c.id) {
                                for (Client c1 : new ArrayList<>(clients)) {
                                    if (c1.getId() == b.playerId) {
                                        c1.removePlayerObject(b);
                                    }
                                }

                                bulletsToRemove.add(b);

                                // Update client health
                                int newHealth = c.getHealth() - 10;
                                c.setHealth(Math.max(newHealth, 0));

                                if (newHealth < 1) {
                                    deadClientIdQue.add(c.id);
                                }

                                break;
                            }
                        }
                    }
                }

                try {
                    playerObjects.removeAll(bulletsToRemove);
                } catch (IndexOutOfBoundsException e) {
                }
            }
        }

    }

}
