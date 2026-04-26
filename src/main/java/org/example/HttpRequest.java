package org.example;

public class HttpRequest {

    private final String method;   // GET, POST, ...
    private final String url;      // http://host:port/path (полный URL от браузера)
    private final String host;     // host
    private final int port;        // 80 по умолчанию
    private final String path;     // /path?query
    private final String version;  // HTTP/1.1
    private final byte[] rawHeaders; // все заголовки как есть

    public HttpRequest(String method, String url, String host, int port,
                       String path, String version, byte[] rawHeaders) {
        this.method = method;
        this.url = url;
        this.host = host;
        this.port = port;
        this.path = path;
        this.version = version;
        this.rawHeaders = rawHeaders;
    }

    public byte[] toServerBytes() {
        String requestLine = method + " " + path + " " + version + "\r\n";
        byte[] lineBytes = requestLine.getBytes();
        byte[] result = new byte[lineBytes.length + rawHeaders.length];
        System.arraycopy(lineBytes, 0, result, 0, lineBytes.length);
        System.arraycopy(rawHeaders, 0, result, lineBytes.length, rawHeaders.length);
        return result;
    }

    public String getMethod()  { return method; }
    public String getUrl()     { return url; }
    public String getHost()    { return host; }
    public int getPort()       { return port; }
    public String getPath()    { return path; }
    public String getVersion() { return version; }
}
