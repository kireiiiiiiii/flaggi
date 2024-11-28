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

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;

/**
 * Server class for the LAN Game application.
 * 
 */
public class Server {

    /////////////////
    // Constants
    ////////////////

    public static final int TCP_PORT = 54321;
    public static final int UDP_PORT = 54322;
    private static final int CLIENT_TIMEOUT_SECONDS = 4;
    private static final List<Client> clients = new ArrayList<>();
    private static int clientNum = 0;

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
        ServerSocket serverSocket = new ServerSocket(TCP_PORT);
        log(YELLOW, "Server socket created on IP: '" + getIPv4Address().getHostAddress() + "'");
        new Thread(() -> startTCPListener(serverSocket)).start();
        log(YELLOW, "Server started on port '" + TCP_PORT + "'. Waiting for clients...");
        new Thread(Server::startUDPListener).start();
        log(YELLOW, "UDP started on port '" + UDP_PORT + "'. Waiting for data...");
    }

    /**
     * Starts the TCP listener that listens for new clients, assigns them their
     * IDs, and gives them the UDP port to send data to.
     * 
     * @param serverSocket - TCP port
     */
    private static void startTCPListener(ServerSocket serverSocket) {
        while (true) {
            try (Socket clientSocket = serverSocket.accept()) {
                clientSocket.setSoTimeout(500);

                try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                        ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

                    String initialMessage = in.readUTF();

                    if ("ping".equals(initialMessage)) {
                        out.writeUTF("pong");
                        out.flush();
                        log(BLACK, "Handled 'is server running' check from client, responded 'pong'");
                        continue;
                    }

                    clientSocket.setSoTimeout(0);

                    int clientId = clientNum++;
                    String clientName = initialMessage;
                    InetAddress clientAddress = clientSocket.getInetAddress();

                    synchronized (clients) {
                        clients.add(new Client(clientId, clientName, clientAddress));
                    }
                    log(GREEN, "Assigned ID '" + clientId + "' to client '" + clientName + "'");

                    out.writeInt(clientId);
                    out.writeInt(UDP_PORT);
                    out.flush();
                    log(PURPLE, "Sent port and ID to client '" + clientName + "'");

                } catch (EOFException | SocketTimeoutException e) {
                    log(RED, e.getMessage());
                } catch (IOException e) {
                    log(RED, "Error handling client connection: " + e.getMessage());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts the UDP listener for the clients to send their position data to.
     * Now also responds to heartbeat messages from clients to confirm connectivity.
     */
    private static void startUDPListener() {
        try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {

            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                String[] parts = message.split(",");
                if (parts.length >= 4) {
                    int clientId = Integer.parseInt(parts[0]);
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);

                    Client client;
                    synchronized (clients) {
                        client = getClient(clientId);
                    }

                    if (client != null) {
                        client.setPosition(x, y);
                        client.updateLastReceivedTime();

                        String playerData = getAllClientsData();
                        byte[] responseBuffer = playerData.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(
                                responseBuffer, responseBuffer.length, client.getInetAddress(), packet.getPort());

                        udpSocket.send(responsePacket);

                    } else {
                        log(RED, "Client not found with ID: " + clientId);
                    }
                } else {
                    log(RED, "Received message doesn't have enough parts");
                }

                checkForDisconnectedClients();

                buffer = new byte[1024];
            }
        } catch (IOException e) {
            log(RED, "Error in UDP listener: " + e.getMessage());
        }
    }

    /**
     * Checks, if any clients reached the timeout limit => disconnected. If so, removes them.
     * 
     */
    private static void checkForDisconnectedClients() {
        long currentTime = System.currentTimeMillis();
        Iterator<Client> iterator = clients.iterator();

        while (iterator.hasNext()) {
            Client client = iterator.next();
            long timeDifference = currentTime - client.getLastReceivedTime();

            if (timeDifference > CLIENT_TIMEOUT_SECONDS * 1000) {
                log(CYAN, "Client '" + client.getDisplayName() + "' disconnected (Timed out).");
                iterator.remove();
            }
        }
    }

    /////////////////
    // Helper methods
    ////////////////

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
    private static String getAllClientsData() {
        StringBuilder positions = new StringBuilder();
        synchronized (clients) {
            for (Client client : clients) {
                positions.append(client.getId()).append(",")
                        .append(client.getX()).append(",")
                        .append(client.getY()).append(",")
                        .append(client.getDisplayName()).append(";");
            }
        }
        return positions.toString();
    }

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
     * Term colors.
     * 
     */
    public static final String BLACK = "\033[0;30m"; // ping
    public static final String RED = "\033[0;31m"; // err
    public static final String GREEN = "\033[0;32m"; // assigned
    public static final String YELLOW = "\033[0;33m"; // init server
    public static final String BLUE = "\033[0;34m"; // recieve
    public static final String PURPLE = "\033[0;35m"; // send
    public static final String CYAN = "\033[0;36m"; // disconnects
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
        private int x;
        private int y;
        private long lastReceivedTime;

        public Client(int id, String displayName, InetAddress inetAddress) {
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

        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public long getLastReceivedTime() {
            return lastReceivedTime;
        }

        public void updateLastReceivedTime() {
            this.lastReceivedTime = System.currentTimeMillis();
        }
    }

}
