/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 2/23/2025
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggi.server.common;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class UdpListener implements Runnable {

    private final int port;
    private final BlockingQueue<DatagramPacket> packetQueue = new LinkedBlockingQueue<>();
    private final PacketRateLimiter rateLimiter = new PacketRateLimiter(TimeUnit.MILLISECONDS.toMillis(50));

    // Constructor --------------------------------------------------------------

    public UdpListener(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        startListener();
    }

    // Accesors -----------------------------------------------------------------

    public BlockingQueue<DatagramPacket> getPacketQueue() {
        return packetQueue;
    }

    // Private ------------------------------------------------------------------

    private void startListener() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                if (rateLimiter.shouldProcessPacket(packet)) {
                    packetQueue.offer(packet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Limits the rate at what UDP packets from clients can be accepted.
     */
    private static class PacketRateLimiter {
        private final Map<InetAddress, Long> lastPacketTimes = new ConcurrentHashMap<>();
        private final long minIntervalMillis;

        public PacketRateLimiter(long minIntervalMillis) {
            this.minIntervalMillis = minIntervalMillis;
        }

        public boolean shouldProcessPacket(DatagramPacket packet) {
            InetAddress address = packet.getAddress();
            long currentTime = System.currentTimeMillis();
            long lastTime = lastPacketTimes.getOrDefault(address, 0L);

            if (currentTime - lastTime > minIntervalMillis) {
                lastPacketTimes.put(address, currentTime);
                return true;
            } else {
                return false;
            }
        }
    }

}
