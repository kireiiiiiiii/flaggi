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

package kireiiiiiiii;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

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

    /////////////////
    // Constructor
    ////////////////

    /**
     * Constructor. Initializes the client with the server address and port.
     * 
     * @param clientName - {@code String} of the client display name.
     */
    public Client(String clientName) {
        // this.serverAddress = getIPv4Address();
        try {
            this.serverAddress = InetAddress.getByName("10.85.120.177");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
        makeConnection(clientName);
        try {
            udpSocket = new DatagramSocket();
        } catch (SocketException e) {
            Logger.addLog("Client Socket Exception caught", e, true);
        }
    }

    /////////////////
    // Events
    ////////////////

    /**
     * Makes the initial connection with the server through TCP.
     * 
     * @param clientName - {@code String} of the client display name.
     */
    private void makeConnection(String clientName) {
        try (
                Socket socket = new Socket(serverAddress, TCP_PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Send the client display name to the server
            out.writeUTF(clientName);
            out.flush();

            // Receive the assigned client ID and UDP port from the server
            clientId = in.readInt();
            udpPort = in.readInt();

            Logger.addLog("Received client ID '" + clientId + "' from server.", true);
            Logger.addLog("Received UDP port '" + udpPort + "' for updates from server.", true);

        } catch (IOException e) {
            Logger.addLog("IOException caught while sending data to server", e, true);
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
            String message = clientId + "," + clientStruct.getX() + "," + clientStruct.getY() + ","
                    + clientStruct.getName();
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, udpPort);
            udpSocket.send(packet);

            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            udpSocket.receive(receivePacket);

            String data = new String(receivePacket.getData(), 0, receivePacket.getLength());
            playerPositions = parsePositions(data);

        } catch (IOException e) {
            Logger.addLog("IOException caught while sending/receiving position data to/from the server", e, true);
        }

        return playerPositions;
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
    private static ArrayList<ClientStruct> parsePositions(String data) {
        ArrayList<ClientStruct> positions = new ArrayList<>();
        String[] playerData = data.split(";");
        for (String entry : playerData) {
            String[] parts = entry.split(",");
            if (parts.length == 4) {
                int posX = Integer.parseInt(parts[1]);
                int posY = Integer.parseInt(parts[2]);
                String displayName = parts[3];
                positions.add(new ClientStruct(posX, posY, displayName));
            } else {
                Logger.addLog("Recieved server message doesn't have 4 parts", true);
            }
        }
        return positions;
    }

    /**
     * Helper method to get the IPv4 adress of the client, to contact the server.
     * 
     * @return - a {@code InterAdress} of the client IPv4.
     */
    private static InetAddress getIPv4Address() {
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
    /// Client struct
    ////////////////

    /**
     * Client variable structure.
     * 
     */
    public static class ClientStruct {

        private int x;
        private int y;
        private String displayName;

        public ClientStruct(int x, int y, String displayName) {
            this.x = x;
            this.y = y;
            this.displayName = displayName;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public String getName() {
            return this.displayName;
        }

        @Override
        public String toString() {
            return this.x + "," + this.y + "," + this.displayName;
        }

    }

}
