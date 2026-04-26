package org.example;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Пишет краткий журнал в консоль.
 * Формат: [HH:mm:ss] METHOD URL → STATUS
 */
public class Logger {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public synchronized void log(String method, String url, int statusCode) {
        String time = LocalTime.now().format(FMT);
        String status = statusCode == -1 ? "???" : String.valueOf(statusCode);
        System.out.printf("[%s] %-6s %s → %s%n", time, method, url, status);
    }
}
