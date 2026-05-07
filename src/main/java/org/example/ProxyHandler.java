package org.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
            logger.log(request.method(), request.url(), new RequestStatus(403, "Forbidden"));
            return;
        }

        try (Socket serverSocket = new Socket(request.host(), request.port())) {
            serverSocket.getOutputStream().write(request.toServerBytes());
            serverSocket.getOutputStream().flush();

            RequestStatus status = relayResponse(serverSocket.getInputStream(), clientOut);
            logger.log(request.method(), request.url(), status);
        }
    }

    private RequestStatus relayResponse(InputStream serverIn, OutputStream clientOut) throws IOException {
        ByteArrayOutputStream statusLineBuffer = new ByteArrayOutputStream();
        int b, prev = -1;
        while ((b = serverIn.read()) != -1) {
            statusLineBuffer.write(b);
            if (prev == '\r' && b == '\n') break;
            prev = b;
        }

        byte[] statusLineBytes = statusLineBuffer.toByteArray();
        clientOut.write(statusLineBytes);


        RequestStatus status = parseStatusString(new String(statusLineBytes).trim());

        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        while ((len = serverIn.read(buf)) != -1) {
            clientOut.write(buf, 0, len);
            clientOut.flush();
        }
        return status;
    }

    private RequestStatus parseStatusString(String statusLine) {
        String[] parts = statusLine.split(" ", 3);
        if (parts.length >= 2) {
            int statusCode = -1;
            try {
                statusCode = Integer.parseInt(parts[1]);
            } catch (NumberFormatException ignored) {
            }
            String statusMessage = "";
            if (parts.length == 3) {
                statusMessage = parts[2];
            }
            return new RequestStatus(statusCode, statusMessage);
        }
        return null;
    }

    public record RequestStatus(int code, String message) {
    }
}
