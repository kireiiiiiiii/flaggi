/*
 * Author: Matěj Šťastný aka Kirei
 * Date created: 2/23/2025
 * Github link: https://github.com/kireiiiiiiii/flaggi
 */

package flaggishared.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logger class for logging messages with different log levels. Supports logging
 * to console with colored output and to a file.
 */
public class Logger {

    private Logger() {
        throw new UnsupportedOperationException("Logger class cannot be instantiated.");
    }

    private static final LogLevel[] IGNORE = { LogLevel.DEBUG, LogLevel.TCPREQUESTS };
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static File logFile = null;

    // Main ----------------------------------------------------------------------

    /**
     * @see Logger.LogLevel
     */
    public static void log(LogLevel level, String message, Exception e) {
        for (LogLevel l : IGNORE) {
            if (level == l)
                return;
        }

        String timestamp = DATE_FORMAT.format(new Date());
        String paddedLevel = String.format("%-" + (getMaxLogLevelLength() + 2) + "s", "[" + level.name() + "]");
        String logMessage = String.format("%s %s %s %s %s", level.getColor(), timestamp, paddedLevel, TermColors.WHITE, message);

        System.out.println(logMessage);

        if (e != null) {
            System.out.println(level.getColor());
            e.printStackTrace(System.out);
            System.out.print(TermColors.WHITE);
        }

        if (logFile != null) {
            try (FileWriter fw = new FileWriter(logFile, true)) {
                fw.write(String.format("%s %s %s %s%n", timestamp, paddedLevel, "", message));
            } catch (IOException caughtE) {
                log(LogLevel.WARN, "IO Exception caught when writing into log file.", caughtE);
            }
        }
    }

    public static void log(LogLevel level, String message) {
        log(level, message, null);
    }

    // Modifiers -----------------------------------------------------------------

    public static void setLogFile(String path) {
        try {
            logFile = new File(path);
            if (!logFile.exists()) {
                logFile.getParentFile().mkdirs();
                logFile.createNewFile();
            }
            try (FileWriter fw = new FileWriter(logFile, false)) {
                fw.write("");
            }
        } catch (IOException e) {
            log(LogLevel.WARN, "IO Exception caught when setting log file.", e);
        }
    }

    // Log types -----------------------------------------------------------------

    public enum LogLevel {
        INFO(TermColors.GREEN), //
        WARN(TermColors.YELLOW), //
        ERROR(TermColors.RED), //
        DEBUG(TermColors.BLUE), //
        CONNECTION(TermColors.CYAN), //
        PING(TermColors.BLACK), //
        TCPREQUESTS(TermColors.PURPLE);

        private final String color;

        LogLevel(String color) {
            this.color = color;
        }

        public String getColor() {
            return color;
        }
    }

    // Private -------------------------------------------------------------------

    private static class TermColors {
        public static final String BLACK = "\033[0;30m";
        public static final String RED = "\033[0;31m";
        public static final String GREEN = "\033[0;32m";
        public static final String YELLOW = "\033[0;33m";
        public static final String BLUE = "\033[0;34m";
        public static final String PURPLE = "\033[0;35m";
        public static final String CYAN = "\033[0;36m";
        public static final String WHITE = "\033[0;37m";
    }

    private static int getMaxLogLevelLength() {
        int maxLength = 0;
        for (LogLevel level : LogLevel.values()) {
            maxLength = Math.max(maxLength, level.name().length());
        }
        return maxLength;
    }

}
