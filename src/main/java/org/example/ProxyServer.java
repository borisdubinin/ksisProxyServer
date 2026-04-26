package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyServer {

    private final int port;
    private final BlacklistFilter blacklist;
    private final Logger logger;

    public ProxyServer(int port, BlacklistFilter blacklist, Logger logger) {
        this.port = port;
        this.blacklist = blacklist;
        this.logger = logger;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port);
             ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            System.out.println("[Proxy] Listening on port " + port);

            while (!serverSocket.isClosed()) {
                Socket client = serverSocket.accept();
                executor.submit(new ProxyHandler(client, blacklist, logger));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Config config = Config.load("config.properties");
        int port = config.getInt("port", 8080);
        String blacklistPath = config.get("blacklist", "blacklist.txt");

        new ProxyServer(port, new BlacklistFilter(blacklistPath), new Logger()).start();
    }
}