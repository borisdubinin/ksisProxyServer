package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

public class BlacklistFilter {

    private final Set<String> entries;

    public BlacklistFilter(String filePath) {
        this.entries = load(filePath);
    }

    private static Set<String> load(String filePath) {
        Path path = Path.of(filePath);
        if (!Files.exists(path)) {
            System.out.println("[Blacklist] File not found: " + filePath + " (no blocking active)");
            return Set.of();
        }
        try (var lines = Files.lines(path)) {
            Set<String> result = lines
                    .map(String::trim)
                    .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                    .map(String::toLowerCase)
                    .collect(Collectors.toUnmodifiableSet());
            System.out.println("[Blacklist] Loaded " + result.size() + " entries from " + filePath);
            return result;
        } catch (IOException e) {
            System.err.println("[Blacklist] Failed to load: " + e.getMessage());
            return Set.of();
        }
    }

    public boolean isBlocked(String host) {
        if (host == null) return false;
        String lower = host.toLowerCase();
        if (entries.contains(lower)) return true;
        return entries.stream().anyMatch(e -> lower.endsWith("." + e));
    }
}
