/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 11/4/2024 (v2 - 2/22/2025)
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggi.server;

import flaggi.server.constants.Constants;

public class Server {

    // Main & Constructor -------------------------------------------------------

    public static void main(String[] args) {
        new Server();
    }

    public Server() {
        System.out.println(Constants.TCP_PORT);
        System.out.println(Constants.UDP_PORT);
    }
}
