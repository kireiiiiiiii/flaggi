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

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import flaggi.App;

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
    private ObjectOutputStream tcpIn;
    private ObjectInputStream tcpOut;
    private Thread tcpListenerThread;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Default constructor.
     * 
     * @param clientName    - display name of the client.
     * @param serverAddress - server address.
     */
    public Client(String clientName, InetAddress serverAddress) {
        this.clientName = clientName;
        this.serverAddress = serverAddress;

        try {
            this.tcpSocket = new Socket(this.serverAddress, TCP_PORT);
            this.tcpIn = new ObjectOutputStream(tcpSocket.getOutputStream());
            this.tcpOut = new ObjectInputStream(tcpSocket.getInputStream());
            this.udpSocket = new DatagramSocket();

            makeConnection();
            startTCPListener();

        } catch (IOException e) {
            App.LOGGER.addLog("IO Exception occured while connecting to the server.", e);
        }
    }

    /////////////////
    // Public Methods
    ////////////////

    /**
     * Updates the position of this player on the server and gets all the positions
     * of other players from the server. This method is called every frame.
     * 
     * @param clientStruct - local player data.
     * @return a data struct with other player data.
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

            if (data.equals("died")) { // TODO Recieve through TCP
                return new RecievedServerDataStruct(true);
            }

            String[] splitData = data.split("\\|");
            playerPositions = parsePositions(splitData[0]);
            objectData = splitData[1];

        } catch (IOException e) {
            App.LOGGER.addLog("IOException caught while sending/receiving position data.", e);
        }

        return new RecievedServerDataStruct(playerPositions, objectData);
    }

    /**
     * Sends a custom message to the server through TCP.
     * 
     * @param message - The message to send.
     */
    public void sendMessageToServer(String message) {
        try {
            tcpIn.writeUTF(message);
            tcpIn.flush();
            App.LOGGER.addLog("Sent message to server: " + message);
        } catch (IOException e) {
            App.LOGGER.addLog("Failed to send message to server.", e);
        }
    }

    /**
     * Disconnects the client from the server.
     */
    public void disconnectFromServer() { // TODO Send through TCP
        try {
            sendMessageToServer("disconnect");
            udpSocket.close();
            tcpSocket.close();
            tcpListenerThread.interrupt();
            App.LOGGER.addLog("Disconnected successfully from server.");
        } catch (IOException e) {
            App.LOGGER.addLog("IOException while disconnecting from server.", e);
        }
    }

    /**
     * Requests the names of all connected idle players.
     * 
     * @return - List of idle player names.
     */
    public void getConnectedIdlePlayers() { // TODO Remove, and call directly from App
        sendMessageToServer("get-idle-clients");
    }

    /**
     * Checks if there is a server running on a specific IP address and port by
     * trying to establish a connection and exchanging a test message.
     * 
     * @param serverAddress - target IP address or hostname as a {@code InetAddress}
     * @param port          - target port number as an {@code int}
     * @return boolean value - {@code true} if a server is running, {@code false}
     *         otherwise
     */
    public static boolean isServerRunning(InetAddress serverAddress, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverAddress, port), 2000);
            socket.setSoTimeout(5000); // 5 sec

            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeUTF("ping");
                out.flush();

                String response = in.readUTF();
                return "flaggi-pong".equals(response);
            }
        } catch (SocketTimeoutException e) {
            App.LOGGER.addLog("Server check timed out.", e);
            return false;
        } catch (IOException e) {
            App.LOGGER.addLog("Failed to communicate with the server.", e);
            return false;
        }
    }

    /////////////////
    // Private Methods
    ////////////////

    /**
     * Makes initial connection with the server through TCP.
     * 
     */
    private void makeConnection() throws IOException {
        tcpIn.writeUTF("new-client:" + clientName);
        tcpIn.flush();

        clientId = tcpOut.readInt();
        udpPort = tcpOut.readInt();

        App.LOGGER.addLog("Assigned Client ID: " + clientId);
        App.LOGGER.addLog("Received UDP Port: " + udpPort);
    }

    /**
     * Starts a separate thread to listen for server-to-client messages.
     * 
     */
    private void startTCPListener() {
        tcpListenerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {

                String serverMessage = null;
                try {
                    if (tcpOut.available() > 0) {
                        serverMessage = tcpOut.readUTF();
                    } else {
                        throw new EOFException("Stream closed or empty.");
                    }
                } catch (IOException e) { // TODO What are we doing here?
                }
                if (serverMessage != null) {
                    System.out.println("Received message from server: " + serverMessage);
                    handleServerMessage(serverMessage);
                }
            }
        });
        tcpListenerThread.start();
    }

    /**
     * Handles messages received from the server.
     * 
     * @param message - server message.
     */
    private void handleServerMessage(String message) { // TODO implement
        System.out.println("Server message received: " + message);
    }

    /**
     * Parses the string of player positions.
     * 
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
            }
        }

        return positions;
    }

    /////////////////
    // Accessors
    ////////////////

    /**
     * Accesor for the server give ID.
     * 
     * @return - client ID.
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
    public static class ClientStruct { // TODO Move to a separate class

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
    public static class RecievedServerDataStruct { // TODO simplify this mess

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
        public RecievedServerDataStruct(boolean isDead) { // TODO death should be through TCP
            this.isDead = isDead;
        }

    }

}
