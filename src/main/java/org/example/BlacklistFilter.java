package org.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class BlacklistFilter {

    private final List<String> entries = new ArrayList<>();

    public BlacklistFilter(String filePath) {
        load(filePath);
    }

    private void load(String filePath) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            System.out.println("[Blacklist] File not found: " + filePath + " (no blocking active)");
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    entries.add(line.toLowerCase());
                }
            }
            System.out.println("[Blacklist] Loaded " + entries.size() + " entries from " + filePath);
        } catch (IOException e) {
            System.err.println("[Blacklist] Failed to load: " + e.getMessage());
        }
    }

    public boolean isBlocked(String hostOrUrl) {
        if (hostOrUrl == null) return false;
        String lower = hostOrUrl.toLowerCase();
        for (String entry : entries) {
            if (lower.equals(entry) || lower.endsWith("." + entry) || lower.contains("/" + entry)
                    || lower.startsWith(entry + "/") || lower.contains(entry)) {
                return true;
            }
        }
        return false;
    }
}
