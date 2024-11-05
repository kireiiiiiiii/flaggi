import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {

    public static final int TCP_PORT = 54321;
    public static final int UDP_PORT = 54322;

    private static final List<Client> clients = new ArrayList<>();
    private static int clientNum = 0;

    public static void main(String[] args) {
        System.out.println("lol");
        try {
            ServerSocket serverSocket = new ServerSocket(TCP_PORT);
            startServer(serverSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startServer(ServerSocket serverSocket) {
        new Thread(() -> startTCPListener(serverSocket)).start();
        System.out.println("Server started on port '" + TCP_PORT + "'. Waiting for clients...");
        new Thread(Server::startUDPListener).start();
    }

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
                System.out.println("Assigned Client ID " + clientId + " to " + clientName);

                out.writeInt(clientId);
                out.writeInt(UDP_PORT);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void startUDPListener() {
        try (DatagramSocket udpSocket = new DatagramSocket(UDP_PORT)) {
            byte[] buffer = new byte[1024];
            System.out.println("UDP listener started on port " + UDP_PORT);

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received packet from client: " + message); // Log received data

                System.out.println("Before split");
                String[] parts = message.split(",");
                System.out.println("After split");
                if (parts.length >= 3) {
                    int clientId = Integer.parseInt(parts[0]);
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);

                    System.out.println("Before sync");
                    Client client;
                    synchronized (clients) {
                        client = getClient(clientId);
                    }
                    System.out.println("After sync");

                    if (client != null) {
                        client.setPosition(x, y);
                        System.out.println("Position updated for Client " + client.getDisplayName());

                        String allPositions = getAllClientPositions();
                        byte[] responseBuffer = allPositions.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(
                                responseBuffer, responseBuffer.length, client.getInetAddress(), packet.getPort()); // packet.getPort()

                        System.out.println("Sending positions to client: " + allPositions); // Log sent data
                        udpSocket.send(responsePacket);
                    } else {
                        System.out.println("Client null");
                    }
                } else {
                    System.out.println("Message not 3 parts");
                }

                Arrays.fill(buffer, (byte) 0); // Clear buffer
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
