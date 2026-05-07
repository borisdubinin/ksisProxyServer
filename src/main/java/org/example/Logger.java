package org.example;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void log(String method, String url, ProxyHandler.RequestStatus status) {
        String statusCode = status.code() == -1 ? "???" : String.valueOf(status.code());
        System.out.printf("[%s] %-6s %s → %s - %s%n", LocalTime.now().format(FMT), method, url, statusCode, status.message());
    }
}
