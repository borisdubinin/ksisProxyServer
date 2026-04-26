package org.example;

import java.io.*;
import java.util.Properties;

public class Config {

    private final Properties props;

    private Config(Properties props) {
        this.props = props;
    }

    public static Config load(String resourceName) throws IOException {
        Properties props = new Properties();
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (in != null) {
                props.load(in);
            } else {
                System.out.println("[Config] Not found in classpath: " + resourceName + " (using defaults)");
            }
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