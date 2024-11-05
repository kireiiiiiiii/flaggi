package org.example;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;

public class Client {

    public static final int TCP_PORT = 54321;
    public static final int UDP_PORT = 54322;

    private int udpPort;
    private int clientId;
    private InetAddress serverAddress;
    private DatagramSocket udpSocket;

    public Client(String clientName) {
        makeConnection(clientName);
        try {
            udpSocket = new DatagramSocket(); // Only create once
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void makeConnection(String clientName) {
        try {
            serverAddress = InetAddress.getByName("192.168.250.158"); // Use your server IP here
            try (
                    Socket socket = new Socket(serverAddress, TCP_PORT);
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeUTF(clientName);
                out.flush();

                // Receive the assigned client ID and UDP port from the server
                clientId = in.readInt();
                udpPort = in.readInt();

                System.out.println("Received Client ID: " + clientId);
                System.out.println("UDP port for updates: " + udpPort);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (UnknownHostException e) {
            System.out.println("Could not resolve server address.");
            e.printStackTrace();
        }
    }

    public ArrayList<int[]> sendPos(int x, int y) {
        ArrayList<int[]> playerPositions = new ArrayList<>();

        try {
            String message = clientId + "," + x + "," + y;
            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, serverAddress, udpPort);

            System.out.println("Sending position to server: " + message); // Log sending
            udpSocket.send(packet);

            // Buffer to receive position updates
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            System.out.println("Waiting for server response..."); // Log waiting
            udpSocket.receive(receivePacket);

            String data = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Received data from server: " + data); // Log received data
            playerPositions = parsePositions(data);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return playerPositions;
    }

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

    public static InetAddress getLocalIPAddress() throws Exception {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp()) {
                continue;
            }
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr.isSiteLocalAddress()) {
                    return addr;
                }
            }
        }
        return null;
    }
}