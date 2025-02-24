/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 2/23/2025
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggi.server.common;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import flaggishared.common.Logger;
import flaggishared.common.Logger.LogLevel;
import flaggishared.flaggi.Message;

public class TcpListener implements Runnable {

    private final int port;
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();

    // Constructor --------------------------------------------------------------

    public TcpListener(int port) {
        this.port = port;
    }

    // Accesors -----------------------------------------------------------------

    public BlockingQueue<Message> getMessageQueue() {
        return messageQueue;
    }

    // Update -------------------------------------------------------------------

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, messageQueue), "Client Handeler Thread").start();
            }
        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "An error occured in the TCP listener.", e);
        }
    }

}
