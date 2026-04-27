package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyServer {

    private static final int PORT = 8080;
    private static final Logger logger = new Logger();

    static void main() throws IOException {
        BlacklistFilter blacklist = new BlacklistFilter("blacklist.txt");
        try (ServerSocket serverSocket = new ServerSocket(PORT);
             ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            System.out.println("[Proxy] Listening on port " + PORT);

            while (!serverSocket.isClosed()) {
                Socket client = serverSocket.accept();
                executor.submit(new ProxyHandler(client, blacklist, logger));
            }
        }
    }
}