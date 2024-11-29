/*
 * Author: Matěj Šťastný
 * Date created: 11/28/2024
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

package flaggi.common;

/**
 * Structure class for the menu options, like last entered name, ip, etc.
 * 
 */
public class AppOptions {

    /////////////////
    // Variables
    ////////////////

    public String name;
    public String ip;

    /////////////////
    // Constructors
    ////////////////

    /**
     * Empty constructor used for the {@code Advanced Variable} class. Don't use
     * this constructor.
     * 
     */
    public AppOptions() {
    }

    /**
     * Default constructor with all parameters.
     * 
     * @param name - user name.
     * @param ip   - server ip.
     */
    public AppOptions(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

}
