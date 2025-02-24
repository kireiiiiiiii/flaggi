/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 11/4/2024 (v2 - 2/22/2025)
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggi.server;

import java.net.DatagramPacket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import flaggi.server.common.TcpListener;
import flaggi.server.common.UdpListener;
import flaggi.server.common.UpdateLoop;
import flaggi.server.common.UpdateLoop.Updatable;
import flaggi.server.constants.Constants;
import flaggishared.common.Logger;
import flaggishared.common.Logger.LogLevel;
import flaggishared.flaggi.Message;

public class Server implements Updatable {

    private final ExecutorService threads;
    private final TcpListener tcpListener;
    private final UdpListener udpListener;
    private final UpdateLoop updateLoop;

    // Main ---------------------------------------------------------------------

    public static void main(String[] args) {
        Server server = new Server();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
    }

    public Server() {
        initializeLogger();

        this.tcpListener = new TcpListener(Constants.TCP_PORT);
        this.udpListener = new UdpListener(Constants.UDP_PORT);
        this.updateLoop = new UpdateLoop(Constants.UPDATE_INTERVAL, this);
        this.threads = Executors.newFixedThreadPool(3);
        initializeThreads();
    }

    // Update -------------------------------------------------------------------

    @Override
    public void update() {
        processTcpMessages(this.tcpListener.getMessageQueue());
        processUdpPackets(this.udpListener.getPacketQueue());
    }

    // Initialization -----------------------------------------------------------

    private void initializeLogger() {
        Logger.setLogFile(Constants.LOG_FILE);
        Logger.setLogLevelsToIgnore(LogLevel.DEBUG, LogLevel.TRACE);
        Logger.log(LogLevel.INFO, "Application start.");
    }

    private void initializeThreads() {
        this.threads.execute(this.tcpListener);
        this.threads.execute(this.udpListener);
        this.threads.execute(this.updateLoop);
    }

    // Private ------------------------------------------------------------------

    private void shutdown() {
        Logger.log(LogLevel.INFO, "Shutting down server...");
        threads.shutdown();
        try {
            if (!threads.awaitTermination(5, TimeUnit.SECONDS)) {
                threads.shutdownNow();
            }
        } catch (InterruptedException e) {
            threads.shutdownNow();
            Thread.currentThread().interrupt();
        }
        Logger.log(LogLevel.INFO, "Server shut down.");
    }

    // Newtwork -----------------------------------------------------------------

    private void processUdpPackets(BlockingQueue<DatagramPacket> packetQueue) {
        DatagramPacket packet;
        while ((packet = packetQueue.poll()) != null) {
            System.out.println("Packet received from: " + packet.getAddress());
            String packetContents = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Packet contents: " + packetContents);
        }
    }

    private void processTcpMessages(BlockingQueue<Message> messageQueue) {
        Message message;
        while ((message = messageQueue.poll()) != null) {
            System.out.println("TCP Message received: " + message.getContent());
        }
    }

}
