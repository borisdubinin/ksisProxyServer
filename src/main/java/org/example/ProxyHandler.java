package org.example;

import java.io.*;
import java.net.Socket;

public class ProxyHandler implements Runnable {

    private static final int BUFFER_SIZE = 8192;

    private final Socket clientSocket;
    private final BlacklistFilter blacklist;
    private final Logger logger;

    public ProxyHandler(Socket clientSocket, BlacklistFilter blacklist, Logger logger) {
        this.clientSocket = clientSocket;
        this.blacklist = blacklist;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            handle();
        } catch (IOException e) {
            // Соединение разорвано — это нормально для потоков
        } finally {
            closeQuietly(clientSocket);
        }
    }

    private void handle() throws IOException {
        InputStream clientIn = clientSocket.getInputStream();
        OutputStream clientOut = clientSocket.getOutputStream();

        // Читаем заголовки запроса от браузера
        HttpRequest request = HttpRequestParser.parse(clientIn);
        if (request == null) return; // Пустой запрос

        String url = request.getUrl();
        String host = request.getHost();
        int port = request.getPort();

        // Проверяем чёрный список
        if (blacklist.isBlocked(host) || blacklist.isBlocked(url)) {
            HttpResponseWriter.writeBlocked(clientOut, url);
            logger.log(request.getMethod(), url, 403);
            return;
        }

        // Подключаемся к серверу назначения
        try (Socket serverSocket = new Socket(host, port)) {
            OutputStream serverOut = serverSocket.getOutputStream();
            InputStream serverIn = serverSocket.getInputStream();

            // Пересылаем запрос: заменяем полный URL на путь (RFC 2616 §5.1.2)
            serverOut.write(request.toServerBytes());
            serverOut.flush();

            // Читаем первую строку ответа для журнала
            int statusCode = readResponseAndRelay(serverIn, clientOut);
            logger.log(request.getMethod(), url, statusCode);
        }
    }

    /**
     * Транслирует байты от сервера к клиенту.
     * Считывает код ответа из первой строки, затем гонит всё остальное потоком.
     * Не буферизует тело — важно для долгих потоков вроде онлайн-радио.
     */
    private int readResponseAndRelay(InputStream serverIn, OutputStream clientOut) throws IOException {
        // Читаем первую строку ответа (статус)
        StringBuilder statusLine = new StringBuilder();
        int b;
        int prev = -1;
        while ((b = serverIn.read()) != -1) {
            statusLine.append((char) b);
            if (prev == '\r' && b == '\n') break;
            prev = b;
        }

        // Пишем строку статуса клиенту
        clientOut.write(statusLine.toString().getBytes());

        int statusCode = parseStatusCode(statusLine.toString());

        // Гоним весь остальной трафик (заголовки + тело) байт за байтом буферами
        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        while ((len = serverIn.read(buf)) != -1) {
            clientOut.write(buf, 0, len);
            clientOut.flush();
        }

        return statusCode;
    }

    private int parseStatusCode(String statusLine) {
        // "HTTP/1.1 200 OK\r\n"
        try {
            String[] parts = statusLine.trim().split(" ");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    private void closeQuietly(Closeable c) {
        try { c.close(); } catch (IOException ignored) {}
    }
}
