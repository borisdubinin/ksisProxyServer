package org.example;

public record HttpRequest(
        String method,
        String url,
        String host,
        int port,
        String path,
        String version,
        byte[] rawHeaders
) {

    public byte[] toServerBytes() {
        byte[] requestLine = (method + " " + path + " " + version + "\r\n").getBytes();

        String headers = new String(rawHeaders)
                .replaceFirst("(?i)Connection:.*?\r\n", "Connection: close\r\n");

        if (!headers.toLowerCase().contains("connection:")) {
            headers = "Connection: close\r\n" + headers;
        }

        byte[] headerBytes = headers.getBytes();
        byte[] result = new byte[requestLine.length + headerBytes.length];
        System.arraycopy(requestLine, 0, result, 0, requestLine.length);
        System.arraycopy(headerBytes, 0, result, requestLine.length, headerBytes.length);
        return result;
    }
}