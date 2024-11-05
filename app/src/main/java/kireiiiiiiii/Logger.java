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

package kireiiiiiiii;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_PATH = "log.txt";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void addLog(String message, boolean append) {
        try (FileWriter fileWriter = new FileWriter(LOG_PATH, append);
                PrintWriter printWriter = new PrintWriter(fileWriter)) {

            String timeStamp = LocalDateTime.now().format(TIME_FORMATTER);
            printWriter.println("[" + timeStamp + "] " + message);

        } catch (IOException e) {
            System.out.println("Error writing to log file: " + e.getMessage());
        }
    }

    public static void addLog(String message, Exception exception, boolean append) {
        try (FileWriter fileWriter = new FileWriter(LOG_PATH, append);
                PrintWriter printWriter = new PrintWriter(fileWriter)) {

            String timeStamp = LocalDateTime.now().format(TIME_FORMATTER);
            printWriter.println("[" + timeStamp + "] " + message);
            if (exception != null) {
                printWriter.println(getStackTraceAsString(exception));
            }

        } catch (IOException e) {
            System.out.println("Error writing to log file: " + getStackTraceAsString(e));
        }
    }

    private static String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

}