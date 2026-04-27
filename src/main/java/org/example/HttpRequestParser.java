package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpRequestParser {

    public static HttpRequest parse(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isBlank()) return null;

        String[] parts = requestLine.split(" ", 3);
        if (parts.length < 3) return null;

        String method  = parts[0];
        String rawUrl  = parts[1];
        String version = parts[2];

        StringBuilder headers = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            headers.append(line).append("\r\n");
        }
        headers.append("\r\n");
        byte[] rawHeaders = headers.toString().getBytes();

        try {
            URI uri = new URI(rawUrl);
            String host = uri.getHost();
            if (host == null) return null;

            int port = uri.getPort() == -1 ? 80 : uri.getPort();
            String path = uri.getRawPath();
            if (path == null || path.isEmpty()) path = "/";
            if (uri.getRawQuery() != null) path += "?" + uri.getRawQuery();

            String url = "http://" + host + (port != 80 ? ":" + port : "") + path;
            return new HttpRequest(method, url, host, port, path, version, rawHeaders);

        } catch (URISyntaxException e) {
            String host = extractHost(headers.toString());
            if (host == null) return null;
            String url = "http://" + host + rawUrl;
            return new HttpRequest(method, url, host, 80, rawUrl, version, rawHeaders);
        }
    }

    private static String extractHost(String headers) {
        for (String line : headers.split("\r\n")) {
            if (line.toLowerCase().startsWith("host:")) {
                String value = line.substring(5).trim();
                int colon = value.indexOf(':');
                return colon != -1 ? value.substring(0, colon) : value;
            }
        }
        return null;
    }
}