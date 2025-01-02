/*
 * Author: Matěj Šťastný
 * Date created: 1/1/2025
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

package flaggiserver.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A logger class for logging messages with different severity levels. The
 * logger uses ANSI escape codes to colorize the output based on the log level.
 * The logger also includes a timestamp formatter to add a timestamp to each log
 * message. The logger supports logging messages with or without an exception.
 * The logger also includes a simple method to log a message without an
 * exception.
 * 
 */
public class Logger {

    /////////////////
    // Constants & Varibles
    ////////////////

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static File logFile = null;

    /////////////////
    // Log methods
    ////////////////

    /**
     * Logs a message with the given log level and optional exception.
     *
     * @param level   - Log level (severity or type of log).
     * @param message - The message to log.
     * @param e       - (Optional) Exception details, if any.
     */
    public static void log(LogLevel level, String message, Exception e) {
        String timestamp = DATE_FORMAT.format(new Date()); // Current time
        String logMessage = String.format("%s %s[%s]%s %s", level.getColor(), // Color based on log level
                timestamp, // Timestamp
                level.name(), // Log level name
                TermColors.WHITE, // Reset color after log level
                message // Actual log message
        );

        System.out.println(logMessage);

        // If an exception is passed, log the stack trace in red
        if (e != null) {
            System.out.println(level.getColor());
            e.printStackTrace(System.out);
            System.out.print(TermColors.WHITE); // Reset to default color
        }

        // Write to log file
        if (logFile != null) {
            try (FileWriter fw = new FileWriter(logFile, true)) {
                String noColorLogMessage = String.format("%s[%s]%s", timestamp, level.name(), message);
                fw.write(noColorLogMessage);
            } catch (IOException caughtE) {
                log(LogLevel.WARN, "IO Exception caught when writing into log file.", caughtE);
            }
        }

    }

    /**
     * Logs a simple message without an exception.
     *
     * @param level   Log level.
     * @param message The message to log.
     */
    public static void log(LogLevel level, String message) {
        log(level, message, null);
    }

    /////////////////
    // Modifiers
    ////////////////

    /**
     * Sets the log file to write to.
     * 
     * @param path
     * @throws IOException
     */
    public static void setLogFile(String path) throws IOException {
        logFile = new File(path);
        if (!logFile.exists()) {
            logFile.getParentFile().mkdirs();
            logFile.createNewFile();
            FileWriter fw = new FileWriter(logFile, false);
            fw.write(" "); // Clear the log file
            fw.close();
        }
    }

    /////////////////
    // Nested
    ////////////////

    /**
     * Enum class to determine the level of the log message and it's ASCII color
     * code.
     * 
     */
    public enum LogLevel {

        INFO(TermColors.GREEN), // General logs
        WARN(TermColors.YELLOW), // Warnings
        ERROR(TermColors.RED), // Errors
        DEBUG(TermColors.BLUE), // Debugging info
        CONNECTION(TermColors.CYAN), // Connections/disconnections
        PING(TermColors.BLACK), // Pings
        TCPREQUESTS(TermColors.PURPLE); // For TCP requests

        private final String color;

        /**
         * Enum constructor for LogLevel.
         * 
         * @param color - Color code for the log level.
         */
        LogLevel(String color) {
            this.color = color;
        }

        /**
         * ASCII color code accessor
         * 
         * @return Color code for the log level.
         */
        public String getColor() {
            return color;
        }

    }

    /**
     * Constant class for ASCII terminal color codes.
     * 
     */
    private static class TermColors {
        public static final String BLACK = "\033[0;30m"; // ping
        public static final String RED = "\033[0;31m"; // err
        public static final String GREEN = "\033[0;32m"; // client handeling logs
        public static final String YELLOW = "\033[0;33m"; // server internal logs
        public static final String BLUE = "\033[0;34m"; // debug
        public static final String PURPLE = "\033[0;35m"; // i dont know?
        public static final String CYAN = "\033[0;36m"; // disconnects /connects
        public static final String WHITE = "\033[0;37m";

    }

}
