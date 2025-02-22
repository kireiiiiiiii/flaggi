/*
 * Author: Matěj Šťastný
 * Date created: 1/5/2025
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

package flaggiclient.sctructs;

import java.util.List;

/**
 * Struct class to hold the data recieved from the server.
 *
 */
public class GameDataStruct {

    // ---- Struct variables
    public List<ClientStruct> connectedClientsList;
    public String playerObjectData;

    /**
     * Default constructor.
     *
     * @param clientList - list of connected clients given by the server.
     * @param objectData - data {@code String} with all player created object data.
     */
    public GameDataStruct(List<ClientStruct> clientList, String objectData) {
        this.connectedClientsList = clientList;
        this.playerObjectData = objectData;
    }

}
