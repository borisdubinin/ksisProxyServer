package org.example;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpRequestParser {

    public static HttpRequest parse(InputStream in) throws IOException {
        // Читаем всё до конца заголовков (\r\n\r\n)
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int[] window = new int[4];
        int b;
        while ((b = in.read()) != -1) {
            buffer.write(b);
            window[0] = window[1];
            window[1] = window[2];
            window[2] = window[3];
            window[3] = b;
            if (window[0] == '\r' && window[1] == '\n'
             && window[2] == '\r' && window[3] == '\n') {
                break;
            }
        }

        byte[] raw = buffer.toByteArray();
        if (raw.length == 0) return null;

        String text = new String(raw);
        String[] lines = text.split("\r\n");
        if (lines.length == 0) return null;

        // Первая строка: "GET http://host/path HTTP/1.1"
        String[] parts = lines[0].split(" ", 3);
        if (parts.length < 3) return null;

        String method  = parts[0];
        String rawUrl  = parts[1];
        String version = parts[2];

        // Разбираем URL
        String host;
        int port;
        String path;
        try {
            URI uri = new URI(rawUrl);
            host = uri.getHost();
            port = uri.getPort() == -1 ? 80 : uri.getPort();

            // Путь + строка запроса
            String p = uri.getRawPath();
            if (p == null || p.isEmpty()) p = "/";
            String q = uri.getRawQuery();
            path = q != null ? p + "?" + q : p;
        } catch (URISyntaxException e) {
            // Fallback: запрос уже в виде пути (нестандартный клиент)
            host = extractHostFromHeaders(lines);
            port = 80;
            path = rawUrl;
        }

        if (host == null) return null;

        // Остальные заголовки (без первой строки)
        // Сохраняем их как есть и добавляем \r\n\r\n
        StringBuilder headers = new StringBuilder();
        for (int i = 1; i < lines.length; i++) {
            headers.append(lines[i]).append("\r\n");
        }
        byte[] rawHeaders = headers.toString().getBytes();

        String fullUrl = "http://" + host + (port != 80 ? ":" + port : "") + path;

        return new HttpRequest(method, fullUrl, host, port, path, version, rawHeaders);
    }

    private static String extractHostFromHeaders(String[] lines) {
        for (String line : lines) {
            if (line.toLowerCase().startsWith("host:")) {
                String value = line.substring(5).trim();
                // Убираем порт если есть
                int colon = value.indexOf(':');
                return colon != -1 ? value.substring(0, colon) : value;
            }
        }
        return null;
    }
}
