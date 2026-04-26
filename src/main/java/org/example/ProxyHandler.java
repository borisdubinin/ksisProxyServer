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
        try (clientSocket) {
            handle();
        } catch (IOException ignored) {
        }
    }

    private void handle() throws IOException {
        HttpRequest request = HttpRequestParser.parse(clientSocket.getInputStream());
        if (request == null) return;

        OutputStream clientOut = clientSocket.getOutputStream();

        if (blacklist.isBlocked(request.host())) {
            HttpResponseWriter.writeBlocked(clientOut, request.url());
            logger.log(request.method(), request.url(), 403);
            return;
        }

        try (Socket serverSocket = new Socket(request.host(), request.port())) {
            serverSocket.getOutputStream().write(request.toServerBytes());
            serverSocket.getOutputStream().flush();

            int statusCode = relayResponse(serverSocket.getInputStream(), clientOut);
            logger.log(request.method(), request.url(), statusCode);
        }
    }

    private int relayResponse(InputStream serverIn, OutputStream clientOut) throws IOException {
        ByteArrayOutputStream statusLineBuffer = new ByteArrayOutputStream();
        int b, prev = -1;
        while ((b = serverIn.read()) != -1) {
            statusLineBuffer.write(b);
            if (prev == '\r' && b == '\n') break;
            prev = b;
        }

        byte[] statusLineBytes = statusLineBuffer.toByteArray();
        clientOut.write(statusLineBytes);

        int statusCode = parseStatusCode(new String(statusLineBytes).trim());

        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        while ((len = serverIn.read(buf)) != -1) {
            clientOut.write(buf, 0, len);
            clientOut.flush();
        }
        return statusCode;
    }

    private int parseStatusCode(String statusLine) {
        String[] parts = statusLine.split(" ", 3);
        if (parts.length >= 2) {
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }
}
