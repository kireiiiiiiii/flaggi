/*
 * Author: Matěj Šťastný
 * Date created: 11/5/2024
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

package kireiiiiiiii.common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logger for the LAN Game application.
 * 
 */
public class Logger {

    /////////////////
    // Constants
    ////////////////

    private static final String LOG_PATH = "log.txt";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /////////////////
    // Methods
    ////////////////

    /**
     * Clears the log file. This method is useful when you want to start logging
     * from a clean state.
     * 
     */
    public static void clearLog() {
        try (FileWriter fileWriter = new FileWriter(LOG_PATH, false)) {
            fileWriter.write("");
        } catch (IOException e) {
            System.out.println("Error clearing log file: " + e.getMessage());
        }
    }

    /**
     * Makes a log. The message will be appended at a new line of the log file.
     * 
     * @param message - log message.
     */
    public static void addLog(String message) {
        try (FileWriter fileWriter = new FileWriter(LOG_PATH, true);
                PrintWriter printWriter = new PrintWriter(fileWriter)) {

            String timeStamp = LocalDateTime.now().format(TIME_FORMATTER);
            printWriter.println("[" + timeStamp + "] " + message);

        } catch (IOException e) {
            System.out.println("Error writing to log file: " + e.getMessage());
        }
    }

    /**
     * Adds an error log.
     * 
     * @param message   - log message.
     * @param exception - {@code Exception} you want to log.
     */
    public static void addLog(String message, Exception exception) {
        try (FileWriter fileWriter = new FileWriter(LOG_PATH, true);
                PrintWriter printWriter = new PrintWriter(fileWriter)) {

            String timeStamp = LocalDateTime.now().format(TIME_FORMATTER);
            printWriter.println("[" + timeStamp + "] " + message + exception.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
