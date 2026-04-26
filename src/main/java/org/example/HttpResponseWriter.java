package org.example;

import java.io.IOException;
import java.io.OutputStream;

public class HttpResponseWriter {

    public static void writeBlocked(OutputStream out, String blockedUrl) throws IOException {
        String body = "<!DOCTYPE html><html><head><meta charset='utf-8'>"
            + "<title>Доступ заблокирован</title>"
            + "<style>body{font-family:sans-serif;text-align:center;padding:60px;color:#333}"
            + "h1{color:#c0392b}code{background:#f4f4f4;padding:4px 8px;border-radius:4px}</style>"
            + "</head><body>"
            + "<h1>&#128683; Доступ заблокирован</h1>"
            + "<p>Адрес <code>" + escapeHtml(blockedUrl) + "</code> заблокирован прокси-сервером.</p>"
            + "</body></html>";

        byte[] bodyBytes = body.getBytes("UTF-8");
        String response = "HTTP/1.1 403 Forbidden\r\n"
            + "Content-Type: text/html; charset=UTF-8\r\n"
            + "Content-Length: " + bodyBytes.length + "\r\n"
            + "Connection: close\r\n"
            + "\r\n";

        out.write(response.getBytes());
        out.write(bodyBytes);
        out.flush();
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
