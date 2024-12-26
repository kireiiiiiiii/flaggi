/*
 * Author: Matěj Šťastný
 * Date created: 11/5/2024
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Multi-thread-safe logger class. Supports multiple log files for different
 * application instances.
 * 
 */
public class Logger {

    /////////////////
    // Constants
    ////////////////

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ConcurrentHashMap<String, Logger> LOGGERS = new ConcurrentHashMap<>();

    /////////////////
    // Variables
    ////////////////

    private final String logPath;

    /////////////////
    // Constructor
    ////////////////

    /**
     * Private constructor.
     * 
     * @param logPath - {@code String} - The file path for the log.
     */
    private Logger(String logPath) {
        this.logPath = logPath;
        File logFile = new File(this.logPath);
        if (!logFile.exists()) {
            try {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating log file: " + e.getMessage());
            }
        }
    }

    /**
     * Gets or creates a logger for a specific log path.
     *
     * @param logPath The file path for the log.
     * @return Logger instance associated with the given path.
     */
    public static Logger getLogger(String logPath) {
        return LOGGERS.computeIfAbsent(logPath, Logger::new);
    }

    /////////////////
    // Log methods
    ////////////////

    /**
     * Clears the log file. This is useful when starting from a clean state.
     */
    public synchronized void clearLog() {
        try (FileWriter fileWriter = new FileWriter(logPath, false)) {
            fileWriter.write("");
        } catch (IOException e) {
            System.err.println("Error clearing log file: " + e.getMessage());
        }
    }

    /**
     * Logs a message with a timestamp.
     *
     * @param message - The log message.
     */
    public synchronized void addLog(String message) {
        try (FileWriter fileWriter = new FileWriter(logPath, true); PrintWriter printWriter = new PrintWriter(fileWriter)) {

            String timeStamp = LocalDateTime.now().format(TIME_FORMATTER);
            printWriter.println("[" + timeStamp + "] " + message);

        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    /**
     * Logs an error message along with exception details.
     *
     * @param message   - The log message.
     * @param exception - The exception to log.
     */
    public synchronized void addLog(String message, Exception exception) {
        try (FileWriter fileWriter = new FileWriter(logPath, true); PrintWriter printWriter = new PrintWriter(fileWriter)) {

            String timeStamp = LocalDateTime.now().format(TIME_FORMATTER);
            printWriter.println("[" + timeStamp + "] " + message);
            printWriter.println("Exception: " + exception.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs a message to the console (optional utility for debugging).
     *
     * @param message - The log message.
     */
    public void logToConsole(String message) {
        String timeStamp = LocalDateTime.now().format(TIME_FORMATTER);
        System.out.println("[" + timeStamp + "] " + message);
    }

}
