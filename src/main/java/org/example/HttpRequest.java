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
        byte[] result = new byte[requestLine.length + rawHeaders.length];
        System.arraycopy(requestLine, 0, result, 0, requestLine.length);
        System.arraycopy(rawHeaders, 0, result, requestLine.length, rawHeaders.length);
        return result;
    }
}