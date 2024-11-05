/*
 * Author: Matěj Šťastný
 * Date created: 4/11/2024
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

public class Client {

    public static final int TCP_PORT = 54321;
    public static final int UDP_PORT = 54322;

    private String serverAdress = "192.168.250.158";
    private int udpPort;
    private int clientId;
    private InetAddress serverAddress;
    private DatagramSocket udpSocket;

    public Client(String clientName) {

        makeConnection(clientName);
        try {
            udpSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace(); // TODO LOG
        }
    }

    private void makeConnection(String clientName) {
        try {
            serverAddress = InetAddress.getByName(this.serverAdress);
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

                System.out.println("Received Client ID: " + clientId); // TODO log
                System.out.println("UDP port for updates: " + udpPort); // TODO log

            } catch (IOException e) {
                e.printStackTrace(); // TODO LOG
            }

        } catch (UnknownHostException e) {
            System.out.println("Server adress " + this.serverAdress + " coudn't be found.");
            e.printStackTrace(); // TODO LOG
        }
    }

    /**
     * Updates the position of this player on the server and gets all the positions
     * of other players from the server. This method is called every frame.
     * 
     * @param playerX - this players X position.
     * @param playerY - this players Y position.
     * @return {@code ArrayList} of positions of other players.
     */
    public ArrayList<int[]> updatePlayerPositions(int playerX, int playerY) {
        ArrayList<int[]> playerPositions = new ArrayList<>();

        try {
            String message = clientId + "," + playerX + "," + playerY;
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, udpPort);

            // System.out.println("Sending position to server: '" + message + "'"); //todo
            udpSocket.send(packet);

            // Buffer to receive position updates
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            // System.out.println("Waiting for server response..."); // TODO LOG
            udpSocket.receive(receivePacket);

            String data = new String(receivePacket.getData(), 0, receivePacket.getLength());
            // System.out.println("Received data from server: " + data); // TODO LOG
            playerPositions = parsePositions(data);

        } catch (IOException e) {
            e.printStackTrace(); // TODO LOG
        }

        return playerPositions;
    }

    /**
     * Parses the string of player positions and converts them to an
     * {@code ArrayList} of position arrays.
     * 
     * @param data - {@code String} of the data.
     * @return - {@code ArrayList} of x,y positions.
     */
    private ArrayList<int[]> parsePositions(String data) {
        ArrayList<int[]> positions = new ArrayList<>();
        String[] playerData = data.split(";");
        for (String entry : playerData) {
            String[] parts = entry.split(",");
            if (parts.length == 3) {
                int posX = (int) Math.round(Double.parseDouble(parts[1]));
                int posY = (int) Math.round(Double.parseDouble(parts[2]));
                positions.add(new int[] { posX, posY });
            }
        }
        return positions;
    }

}
