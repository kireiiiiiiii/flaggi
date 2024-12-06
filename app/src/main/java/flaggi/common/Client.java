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
import java.util.Enumeration;

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
     * Makes the initial connection with the server through TCP.
     * 
     * @param clientName - {@code String} of the client display name.
     */
    private void makeConnection(String clientName) {
        try (Socket socket = new Socket(serverAddress, TCP_PORT); ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

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
     * Updates the position of this player on the server and gets all the positions
     * of other players from the server. This method is called every frame.
     * 
     * @param clientStruct - {@code ClientStruct} object containing the new position
     *                     of this player.
     * @return {@code ArrayList} of positions of other players.
     */
    public ArrayList<ClientStruct> updatePlayerPositions(ClientStruct clientStruct) {

        ArrayList<ClientStruct> playerPositions = new ArrayList<>();

        try {
            String message = clientId + "," + clientStruct.getX() + "," + clientStruct.getY() + "," + clientStruct.getName() + "," + clientStruct.getAnimationFrame();
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, udpPort);
            udpSocket.send(packet);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            udpSocket.receive(receivePacket);

            String data = new String(receivePacket.getData(), 0, receivePacket.getLength());
            playerPositions = parsePositions(data, this.clientId);

        } catch (IOException e) {
            App.LOGGER.addLog("IOException caught while sending/receiving position data to/from the server", e);
        }

        return playerPositions;
    }

    /**
     * Disconnects the player from the server.
     * 
     */
    public void disconnect() {
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
     * Parses the string of player positions and converts them to an
     * {@code ArrayList} of position arrays.
     * 
     * @param data - {@code String} of the data.
     * @return - {@code ArrayList} of {@code ClientStructs}.
     */
    private static ArrayList<ClientStruct> parsePositions(String data, int excludeID) {
        ArrayList<ClientStruct> positions = new ArrayList<>();
        String[] playerData = data.split(";");
        for (String entry : playerData) {
            String[] parts = entry.split(",");
            if (parts.length == 5) {
                int clientID = Integer.parseInt(parts[0]);
                int posX = Integer.parseInt(parts[1]);
                int posY = Integer.parseInt(parts[2]);
                String displayName = parts[3];
                String animationFrame = parts[4];

                // Exclude the local player
                if (clientID != excludeID) {
                    positions.add(new ClientStruct(posX, posY, clientID, displayName, animationFrame));
                }
            } else {
                App.LOGGER.addLog("Recieved server message doesn't have 5 parts");
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
     * Client variable structure.
     * 
     */
    public static class ClientStruct {

        private int x, y, id;
        private String displayName, animationFrame;

        public ClientStruct(int x, int y, int id, String displayName, String animationName) {
            this.x = x;
            this.y = y;
            this.id = id;
            this.displayName = displayName;
            this.animationFrame = animationName;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.displayName;
        }

        public String getAnimationFrame() {
            return this.animationFrame;
        }

        @Override
        public String toString() {
            return this.x + "," + this.y + "," + this.displayName;
        }

    }

}
