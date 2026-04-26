package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class Config {

    private final Properties props;

    private Config(Properties props) {
        this.props = props;
    }

    public static Config loadFromClasspath(String resourceName) throws IOException {
        Properties props = new Properties();
        InputStream in = Config.class.getClassLoader().getResourceAsStream(resourceName);
        if (in != null) {
            try (in) {
                props.load(in);
            }
        } else {
            System.out.println("[Config] Resource not found in classpath: " + resourceName + " (using defaults)");
        }
        return new Config(props);
    }

    public static Config load(String filePath) throws IOException {
        Properties props = new Properties();
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                props.load(in);
            }
        } else {
            System.out.println("[Config] File not found: " + filePath + " (using defaults)");
        }
        return new Config(props);
    }

    public String get(String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    public int getInt(String key, int defaultValue) {
        String val = props.getProperty(key);
        if (val == null) return defaultValue;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}