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

package flaggi.common;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import flaggi.App;

/**
 * Client class for communicating with the server.
 * 
 */
public class Client {

    /////////////////
    // Constants
    ////////////////

    public static final int TCP_PORT = 54321;

    /////////////////
    // Variables
    ////////////////

    private int udpPort;
    private int clientId;
    private InetAddress serverAddress;
    private Socket tcpSocket;
    private DatagramSocket udpSocket;
    private String clientName;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Constructor. Initializes the client with the server address and port.
     * 
     * @param clientName - {@code String} of the client display name.
     */
    public Client(String clientName, InetAddress serverAddress) {

        // ------ Set variables
        this.clientName = clientName;
        this.serverAddress = serverAddress;

        // ------ Socket creation
        try {
            this.tcpSocket = new Socket(this.serverAddress, TCP_PORT);
        } catch (IOException e) {
            App.LOGGER.addLog("Client IOException caught when creating TCP socket.", e);
        }

        // ------ Make the connection
        makeConnection(this.clientName);
        try {
            udpSocket = new DatagramSocket();
        } catch (SocketException e) {
            App.LOGGER.addLog("Client Socket Exception caught", e);
        }
    }

    /////////////////
    // Public methods
    ////////////////

    /**
     * Updates the position of this player on the server and gets all the positions
     * of other players from the server. This method is called every frame.
     * 
     * @param clientStruct - {@code ClientStruct} object containing the new position
     *                     of this player.
     * @return {@code ArrayList} of positions of other players.
     */
    public RecievedServerDataStruct updatePlayerPositions(ClientStruct clientStruct) {

        ArrayList<ClientStruct> playerPositions = new ArrayList<>();
        String objectData = "";

        try {
            String message = clientStruct.toString();
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, udpPort);
            udpSocket.send(packet);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            udpSocket.receive(receivePacket);

            String data = new String(receivePacket.getData(), 0, receivePacket.getLength());

            // ---- SPECIAL CASES
            if (data.equals("died")) {
                return new RecievedServerDataStruct(true);
            }

            String[] splitData = new String[] { data.substring(0, data.indexOf("|")), data.substring(data.indexOf("|") + 1) };
            String playerData = splitData[0];
            objectData = splitData[1];
            playerPositions = parsePositions(playerData);

        } catch (IOException e) {
            App.LOGGER.addLog("IOException caught while sending/receiving position data to/from the server", e);
        }

        return new RecievedServerDataStruct(playerPositions, objectData);
    }

    /**
     * Disconnects the player from the server.
     * 
     */
    public void disconnectFromServer() {
        String message = this.clientId + "," + "disconnect";
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, udpPort);
        try {
            udpSocket.send(packet);
            App.LOGGER.addLog("Disconnected succesfuly from server.");
        } catch (IOException e) {
            App.LOGGER.addLog("IOExeption while sending disconnect message to server.", e);
        }
        udpSocket.close();
    }

    /**
     * Checks if there is a server running on a specific IP address and port by
     * trying to establish a connection and exchanging a test message.
     * 
     * @param serverAddress - target IP address or hostname as a String
     * @param port          - target port
     * @return boolean value, true if something is running on the specified IP and
     *         port
     */
    public static boolean isServerRunning(InetAddress serverAddress, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverAddress, port), 2000); // 2-second timeout for connection

            socket.setSoTimeout(5000);

            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeUTF("ping");
                out.flush();

                String response = in.readUTF();
                return "pong".equals(response);
            }
        } catch (SocketTimeoutException e) {
            App.LOGGER.addLog("Server running check timed out.", e);
            return false;
        } catch (IOException e) {
            App.LOGGER.addLog("Server running check failed.", e);
            return false;
        }
    }

    /**
     * Requests the names of all connected players from the server.
     * 
     * @return - {@code List} of {@code String}s for all idle connected players from
     *         the server.
     */
    public List<String> getConnectedIdlePlayers() {

        List<String> connectedIdlePlayers = new ArrayList<>();

        try {

            this.tcpSocket = new Socket(this.serverAddress, TCP_PORT);
            ObjectOutputStream out = new ObjectOutputStream(this.tcpSocket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(this.tcpSocket.getInputStream());

            out.writeUTF("get-idle-clients");
            out.flush();

            String data = in.readUTF();
            if (data != null && !data.isEmpty()) {
                connectedIdlePlayers = Arrays.asList(data.split(","));
            }

        } catch (IOException e) {
            App.LOGGER.addLog("IOException caught while communicating with server", e);
        }

        return connectedIdlePlayers;
    }

    /**
     * Helper method to get the IPv4 adress of the client, to contact the server.
     * 
     * @return - a {@code InterAdress} of the client IPv4.
     */
    public static InetAddress getIPv4Address() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // Skip loopback and inactive interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();

                    // Check for an IPv4 address
                    if (inetAddress instanceof java.net.Inet4Address) {
                        return inetAddress; // Return the InetAddress instance
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null; // No IPv4 address found
    }

    /////////////////
    // Helper methods
    ////////////////

    /**
     * Makes the initial connection with the server through TCP.
     * 
     * @param clientName - {@code String} of the client display name.
     */
    private void makeConnection(String clientName) {
        try (ObjectOutputStream out = new ObjectOutputStream(this.tcpSocket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(this.tcpSocket.getInputStream())) {

            // ------ Send the client name to the server
            out.writeUTF(clientName);
            out.flush();

            // ------ Receive the assigned client ID and UDP port from the server
            clientId = in.readInt();
            App.LOGGER.addLog("Received client ID '" + clientId + "' from server.");

            udpPort = in.readInt();
            App.LOGGER.addLog("Received UDP port '" + udpPort + "' for updates from server.");

        } catch (IOException e) {
            App.LOGGER.addLog("IOException caught while sending data to server", e);
        }
    }

    /**
     * Parses the string of player positions and converts them to an
     * {@code ArrayList} of position arrays.
     * 
     * @param data - {@code String} of the data.
     * @return - {@code ArrayList} of {@code ClientStructs}.
     */
    private static ArrayList<ClientStruct> parsePositions(String data) {
        ArrayList<ClientStruct> positions = new ArrayList<>();
        String[] playerData = data.split(";");
        for (String entry : playerData) {
            String[] parts = entry.split(",");
            if (parts.length == 6) {
                int clientID = Integer.parseInt(parts[0]);
                int posX = Integer.parseInt(parts[1]);
                int posY = Integer.parseInt(parts[2]);
                int health = Integer.parseInt(parts[3]);
                String displayName = parts[4];
                String animationFrame = parts[5];
                positions.add(new ClientStruct(posX, posY, clientID, health, displayName, animationFrame, ""));
            } else {
                App.LOGGER.addLog("Recieved server message doesn't have 6 parts");
            }
        }
        return positions;
    }

    /////////////////
    /// Accesors
    ////////////////

    /**
     * Returns the id of the client given by the server.
     * 
     * @return {@code int} of the client ID.
     */
    public int getId() {
        return this.clientId;
    }

    /////////////////
    /// Client struct
    ////////////////

    /**
     * A read only structure class for a client.
     * 
     */
    public static class ClientStruct {

        // ---- Struct variables
        private int x, y, id, health;
        private String displayName, animationFrame, playerObjectData;

        /**
         * Default constructor
         * 
         * @param x                - X position of the client.
         * @param y                - Y position of the client.
         * @param id               - ID of the client given by the server.
         * @param health           - current health of the client.
         * @param displayName      - display name of the client.
         * @param animationName    - animation frame data.
         * @param playerObjectData - player objects data.
         */
        public ClientStruct(int x, int y, int id, int health, String displayName, String animationName, String playerObjectData) {
            this.x = x;
            this.y = y;
            this.id = id;
            this.health = health;
            this.displayName = displayName;
            this.animationFrame = animationName;
            this.playerObjectData = playerObjectData;
        }

        /**
         * Accesor for the X coordinate of the client.
         * 
         * @return X coordinate of the client.
         */
        public int getX() {
            return this.x;
        }

        /**
         * Accesor for the Y coordinate of the client.
         * 
         * @return Y coordinate of the client.
         */
        public int getY() {
            return this.y;
        }

        /**
         * Accesor for the ID of the client.
         * 
         * @return id of the client.
         */
        public int getId() {
            return this.id;
        }

        /**
         * Accesor for the health of the client.
         * 
         * @return health of the client.
         */
        public int getHealth() {
            return this.health;
        }

        /**
         * Accesor for the display name of the client.
         * 
         * @return display name of the client.
         */
        public String getName() {
            return this.displayName;
        }

        /**
         * Accesor for the animation frame of the client.
         * 
         * @return animation frame data.
         */
        public String getAnimationFrame() {
            return this.animationFrame;
        }

        /**
         * Accesor for the player objects data of the client.
         * 
         * @return player objects data.
         */
        public String getPlayerObjectData() {
            return this.playerObjectData;
        }

        /**
         * To String method used to get the data {@code String} to send to server.
         * 
         */
        @Override
        public String toString() {
            return this.id + "," + this.x + "," + this.y + "," + this.health + "," + this.displayName + "," + this.animationFrame + "," + this.playerObjectData;
        }

    }

    /////////////////
    /// Recieved server data struct
    ////////////////

    /**
     * Struct class to hold the data recieved from the server.
     * 
     */
    public static class RecievedServerDataStruct {

        // ---- Struct variables
        public List<ClientStruct> connectedClientsList;
        public String playerObjectData;
        public boolean isDead;

        /**
         * Default constructor.
         * 
         * @param list       - list of connected clients given by the server.
         * @param objectData - data {@code String} with all player created object data.
         */
        public RecievedServerDataStruct(List<ClientStruct> list, String objectData) {
            this.connectedClientsList = list;
            this.playerObjectData = objectData;
        }

        /**
         * Returned when server reported player died.
         * 
         * @param isDead - {@code boolean} value, true if player died.
         */
        public RecievedServerDataStruct(boolean isDead) {
            this.isDead = isDead;
        }

    }

}
