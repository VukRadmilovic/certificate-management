package ftn.app.util;

import ftn.app.model.enums.EventType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LoggingUtil {

    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String  ANSI_RESET = "\u001B[0m";
    public static void LogEvent(String user, EventType eventType, String message) {

        LocalDateTime timestamp = LocalDateTime.now();
        String date = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(timestamp);
        if(user.equals("")) user = "Unauthorized user";
        else user = "User " + user;
        String finalUser = user;
        System.out.println(ANSI_BLUE + timestamp + ANSI_RESET + "\t" +
                ANSI_GREEN + eventType + ANSI_RESET + "\t" + ": " +
                ANSI_YELLOW + user + " " + message + "." + ANSI_RESET);
        new Thread(() -> {
            String path = "src/main/java/ftn/app/util/logs/log_" + date + ".txt";
            File f = new File(path);
            if(!f.exists()) f = new File(path);
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path, true))) {
                if(f.length() <= 0) {
                    writer.write("TIMESTAMP\tEVENT_TYPE\tMESSAGE");
                    writer.newLine();
                }
                String line = timestamp + "\t" + eventType + "\t: " + finalUser + " " + message + ".";
                writer.write(line.toString());
                writer.newLine();
            } catch (IOException e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }).start();
    }
}
