package org.example;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void log(String method, String url, int statusCode) {
        String status = statusCode == -1 ? "???" : String.valueOf(statusCode);
        System.out.printf("[%s] %-6s %s → %s%n", LocalTime.now().format(FMT), method, url, status);
    }
}
