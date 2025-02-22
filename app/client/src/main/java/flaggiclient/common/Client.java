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

package flaggiclient.common;

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

import flaggiclient.App;
import flaggiclient.sctructs.ClientStruct;
import flaggiclient.sctructs.GameDataStruct;

public class Client {

    /////////////////
    // Constants
    ////////////////

    public static final int TCP_PORT = 54321;
    private static final int SERVER_TIMEOUT_SECONDS = 3;

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
    private ServerMessageHandeler handeler;

    /////////////////
    // Server requests
    ////////////////

    /**
     * Contstants class with message options for the server.
     *
     */
    public static class ServerRequests {

        public static final String PING = "ping";
        public static final String DISCONNECT = "disconnect";
        public static final String GET_IDLE_CLIENTS = "get-idle-clients";
        public static final String GO_IDLE = "go-idle";

        public static String initialMessage(String clientName) {
            return "new-client:" + clientName;
        }

        public static String invitePlayer(int clientId) {
            return "invite-player:" + clientId;
        }

        public static String acceptInvite(int clientId) {
            return "accept-invite:" + clientId;
        }

        public static String declineInvite(int clientId) {
            return "decline-invite:" + clientId;
        }

    }

    public static class ServerResponses {

        public static final String GO_IDLE = "go-idle";
        public static final String UDP_IDLE = "idle";
        public static final String FLAG_GRAB = "flag-grabbed";

        public static boolean isEnterGame(String message) {
            return message.startsWith("enter-game");
        }

        public static boolean isPong(String message) {
            return message.equals("flaggi-pong");
        }

        public static boolean isDied(String message) {
            return message.startsWith("player-died:");
        }

        public static String isLobbyList(String message) {
            String prefix = "idle-clients:";
            if (message.startsWith(prefix)) {
                if (message.length() == prefix.length()) {
                    return "";
                }
                return message.substring(prefix.length());
            }
            return null;
        }

    }

    /////////////////
    // Constructor
    ////////////////

    /**
     * Default constructor.
     *
     * @param clientName    - display name of the client.
     * @param serverAddress - server address.
     */
    public Client(String clientName, InetAddress serverAddress, ServerMessageHandeler handeler) {
        this.clientName = clientName;
        this.serverAddress = serverAddress;
        this.handeler = handeler;

        try {
            this.tcpSocket = new Socket(this.serverAddress, TCP_PORT);
            this.tcpIn = new ObjectOutputStream(tcpSocket.getOutputStream());
            this.tcpOut = new ObjectInputStream(tcpSocket.getInputStream());
            this.udpSocket = new DatagramSocket();
            this.udpSocket.setSoTimeout(SERVER_TIMEOUT_SECONDS * 1000);

            makeConnection();
            startTCPListener();

        } catch (IOException e) {
            App.LOGGER.addLog("IO Exception occured while connecting to the server.", e);
        }
    }

    /////////////////
    // TCP
    ////////////////

    /**
     * Checks if there is a server running on a specific IP address and port by
     * trying to establish a connection and exchanging a test message.
     *
     * @param serverAddress - target IP address or hostname as a {@code InetAddress}
     * @param port          - target port number as an {@code int}
     * @return boolean value - {@code true} if a server is running, {@code false}
     *         otherwise
     */
    public static boolean isFlaggiServerRunning(InetAddress serverAddress, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(serverAddress, port), 2000);
            socket.setSoTimeout(5000); // 5 sec

            try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeUTF(ServerRequests.PING);
                out.flush();

                String response = in.readUTF();
                return ServerResponses.isPong(response);
            }
        } catch (SocketTimeoutException e) {
            App.LOGGER.addLog("Server check timed out.", e);
            return false;
        } catch (IOException e) {
            App.LOGGER.addLog("Failed to communicate with the server.", e);
            return false;
        }
    }

    /**
     * Starts a separate thread to listen for server-to-client messages.
     *
     */
    private void startTCPListener() {
        tcpListenerThread = new Thread(() -> {
            try {

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (tcpOut.available() > 0) {
                            String serverMessage = tcpOut.readUTF();
                            handeler.handleMessage(serverMessage);
                        } else {
                            Thread.sleep(50);
                        }
                    } catch (EOFException e) {
                        System.err.println("Connection closed by server.");
                    } catch (SocketTimeoutException e) {
                        System.err.println("Socket read timed out. Retrying...");
                    } catch (IOException e) {
                        System.err.println("I/O error while reading from server: " + e.getMessage());
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // TODO Clenup resources
            }
        });

        tcpListenerThread.setDaemon(true);
        tcpListenerThread.start();
    }

    /**
     * Makes initial connection with the server through TCP.
     *
     */
    private void makeConnection() throws IOException {
        tcpIn.writeUTF(ServerRequests.initialMessage(clientName));
        tcpIn.flush();

        clientId = tcpOut.readInt();
        udpPort = tcpOut.readInt();

        App.LOGGER.addLog("Assigned Client ID: " + clientId);
        App.LOGGER.addLog("Received UDP Port: " + udpPort);
    }

    /**
     * Sends a custom message to the server through TCP.
     *
     * @param message - The message to send.
     */
    public void sendTCPMessageToServer(String message) {
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
    public void disconnectFromServer() {
        try {
            sendTCPMessageToServer(ServerRequests.DISCONNECT);
            udpSocket.close();
            tcpSocket.close();
            tcpListenerThread.interrupt();
            App.LOGGER.addLog("Disconnected successfully from server.");
        } catch (IOException e) {
            App.LOGGER.addLog("IOException while disconnecting from server.", e);
        }
    }
    /////////////////
    // UDP
    ////////////////

    /**
     * Updates the position of this player on the server and gets all the positions
     * of other players from the server. This method is called every frame.
     *
     * @param clientStruct - local player data.
     * @return a data struct with other player data.
     */
    public GameDataStruct updatePlayerPositions(ClientStruct clientStruct) {

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
            if (data.equals(ServerResponses.UDP_IDLE)) {
                return null;
            }

            String[] splitData = data.split("\\|");
            playerPositions = parsePositions(splitData[0]);
            objectData = splitData[1];

        } catch (SocketTimeoutException e) {
            this.handeler.timeout();
        } catch (IOException e) {
            App.LOGGER.addLog("IOException caught while sending/receiving position data.", e);
        }

        return new GameDataStruct(playerPositions, objectData);
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
    // Helpers
    ////////////////

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
    // Message handeler interface
    ////////////////

    /**
     * The interface for handling server messages.
     *
     */
    public interface ServerMessageHandeler {

        /**
         * Handeles messages send by the server.
         *
         * @param message - raw server message.
         */
        public void handleMessage(String message);

        /**
         * Method ececuted on server time-out.
         *
         */
        public void timeout();

    }

}
