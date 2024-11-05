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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    /////////////////
    // Variables
    ////////////////

    private static final List<Client> clients = new ArrayList<>();
    private static int clientNum = 0;

    /////////////////
    // Main
    ////////////////

    public static void main(String[] args) {
        try {
            startServer();
        } catch (IOException e) {
            e.printStackTrace();
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
        new Thread(() -> startTCPListener(serverSocket)).start();
        System.out.println("Server started on port '" + TCP_PORT + "'. Waiting for clients...");
        new Thread(Server::startUDPListener).start();
    }

    /**
     * Starts the TCP listener that listens for new clients, and asigns them their
     * IDs and gives them the UDP port to send data to.
     * 
     * @param serverSocket - TCP port
     */
    private static void startTCPListener(ServerSocket serverSocket) {
        while (true) {
            try (
                    Socket clientSocket = serverSocket.accept();
                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

                String clientName = in.readUTF();
                int clientId = clientNum++;

                InetAddress clientAddress = clientSocket.getInetAddress();

                synchronized (clients) {
                    clients.add(new Client(clientId, clientName, clientAddress));
                }
                System.out.println("Assigned ID '" + clientId + "'' to client '" + clientName + "'.");

                out.writeInt(clientId);
                out.writeInt(UDP_PORT);
                out.flush();
                System.out.println("Send port and ID to client'" + clientName + "'.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts the UDP listener for the clients to send their position data to.
     * 
     */
    private static void startUDPListener() {
        boolean log = false;
        try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {

            byte[] buffer = new byte[1024];
            if (log) {
                System.out.println("UDP listener started on port " + UDP_PORT);
            }

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                if (log) {
                    System.out.println("Received packet from client: " + message);
                }

                String[] parts = message.split(",");
                if (parts.length >= 3) {
                    int clientId = Integer.parseInt(parts[0]);
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);

                    Client client;
                    synchronized (clients) {
                        client = getClient(clientId);
                    }

                    if (client != null) {
                        client.setPosition(x, y);
                        if (log) {
                            System.out.println("Position updated for client " + client.getDisplayName());
                        }

                        String allPositions = getAllClientPositions();
                        byte[] responseBuffer = allPositions.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(
                                responseBuffer, responseBuffer.length, client.getInetAddress(), packet.getPort());

                        if (log) {
                            System.out.println("Sending positions to client: " + allPositions);
                        }

                        udpSocket.send(responsePacket);

                    } else {
                        if (log) {
                            System.out.println("Client null");
                        }
                    }
                } else {
                    if (log) {
                        System.out.println("Message not 3 parts");
                    }
                }

                Arrays.fill(buffer, (byte) 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /////////////////
    // Helper methods
    ////////////////

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
    private static String getAllClientPositions() {
        StringBuilder positions = new StringBuilder();
        synchronized (clients) {
            for (Client client : clients) {
                positions.append(client.getId()).append(",")
                        .append(client.getX()).append(",")
                        .append(client.getY()).append(";");
            }
        }
        return positions.toString();
    }

    /////////////////
    // Client struct
    ////////////////

    private static class Client {
        private final int id;
        private final String displayName;
        private final InetAddress inetAddress;
        private float x;
        private float y;

        public Client(int id, String displayName, InetAddress inetAddress) {
            this.id = id;
            this.displayName = displayName;
            this.inetAddress = inetAddress;
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

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public void setPosition(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
