package org.example;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class HttpResponseWriter {

    public static void writeBlocked(OutputStream out, String blockedUrl) throws IOException {
        String body = """
                <!DOCTYPE html>
                <html><head><meta charset="utf-8"><title>Доступ заблокирован</title>
                <style>
                  body { font-family: sans-serif; text-align: center; padding: 60px; color: #333 }
                  h1   { color: #c0392b }
                  code { background: #f4f4f4; padding: 4px 8px; border-radius: 4px }
                </style></head>
                <body>
                  <h1>&#128683; Доступ заблокирован</h1>
                  <p>Адрес <code>%s</code> заблокирован прокси-сервером.</p>
                </body></html>
                """.formatted(escapeHtml(blockedUrl));

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);

        String headers = "HTTP/1.1 403 Forbidden\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Length: %d\r\n".formatted(bodyBytes.length)
                + "Connection: close\r\n\r\n";

        out.write(headers.getBytes(StandardCharsets.UTF_8));
        out.write(bodyBytes);
        out.flush();
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
