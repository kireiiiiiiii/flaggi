/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 2/23/2025
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggi.server.common;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import flaggishared.common.Logger;
import flaggishared.common.Logger.LogLevel;
import flaggishared.flaggi.Message;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final BlockingQueue<Message> messageQueue;

    // Constructor --------------------------------------------------------------

    public ClientHandler(Socket clientSocket, BlockingQueue<Message> messageQueue) {
        this.clientSocket = clientSocket;
        this.messageQueue = messageQueue;
    }

    // Update -------------------------------------------------------------------

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream()); ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            handleInitialMessage(in, out);

            Message message;
            while ((message = (Message) in.readObject()) != null) {
                messageQueue.offer(message);
            }

        } catch (Exception e) {
            Logger.log(LogLevel.ERROR, "An exception occurred in ClientHandler.", e);
        }
    }

    // Private ------------------------------------------------------------------

    private void handleInitialMessage(ObjectInputStream in, ObjectOutputStream out) throws Exception {
        Message initialMessage = (Message) in.readObject();

        if (initialMessage.getType().equals("ping")) {
            out.writeObject(new Message("pong", ""));
            clientSocket.close();
            return;
        }

        if (!initialMessage.getType().equals("name")) {
            out.writeObject(new Message("error", "Invalid initial message. Connection will be closed."));
            clientSocket.close();
            return;
        }

        String uniqueId = UUID.randomUUID().toString();
        out.writeObject(new Message("id", uniqueId));

        initialMessage.setContent(uniqueId);
        messageQueue.offer(initialMessage);
    }
}
