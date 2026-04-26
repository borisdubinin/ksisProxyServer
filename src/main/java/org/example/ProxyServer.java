package org.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Proxy] Listening on port " + port);
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(new ProxyHandler(client, blacklist, logger)).start();
            }
        }
    }

    static void main(String[] args) throws IOException {
        Config config = Config.loadFromClasspath("config.properties");
        int port = config.getInt("port", 8080);
        String blacklistPath = config.get("blacklist", "blacklist.txt");

        BlacklistFilter blacklist = new BlacklistFilter(blacklistPath);
        Logger logger = new Logger();

        new ProxyServer(port, blacklist, logger).start();
    }
}