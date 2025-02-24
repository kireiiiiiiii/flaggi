/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 2/23/2025
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggi.server.common;

import java.net.Socket;

public class ClientMessage {

    private final Socket clientSocket;
    private final String name;
    private final String clientId;
    private String message;

    public ClientMessage(Socket clientSocket, String name, String clientId) {
        this.clientSocket = clientSocket;
        this.name = name;
        this.clientId = clientId;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public String getName() {
        return name;
    }

    public String getClientId() {
        return clientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
