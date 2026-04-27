package org.example;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class BlacklistFilter {

    private final Path path;
    private final AtomicReference<Set<String>> entries = new AtomicReference<>(Set.of());

    public BlacklistFilter(String filePath) throws IOException {
        this.path = Path.of(filePath).toAbsolutePath();
        ensureFileExists();
        entries.set(load());
        startWatcher();
    }

    private void ensureFileExists() throws IOException {
        if (!Files.exists(path)) {
            Files.createFile(path);
            System.out.println("[Blacklist] Created empty file: " + path);
        }
    }

    private Set<String> load() {
        try (var lines = Files.lines(path)) {
            Set<String> result = lines
                    .map(String::trim)
                    .filter(l -> !l.isEmpty() && !l.startsWith("#"))
                    .map(String::toLowerCase)
                    .collect(Collectors.toUnmodifiableSet());
            System.out.println("[Blacklist] Loaded " + result.size() + " entries from " + path);
            return result;
        } catch (IOException e) {
            System.err.println("[Blacklist] Failed to load: " + e.getMessage());
            return Set.of();
        }
    }

    private void startWatcher() throws IOException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        path.getParent().register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);

        Thread.ofVirtual().name("blacklist-watcher").start(() -> {
            while (true) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                boolean changed = key.pollEvents().stream()
                        .map(e -> (Path) e.context())
                        .anyMatch(p -> p.equals(path.getFileName()));

                if (changed) {
                    entries.set(load());
                }

                if (!key.reset()) break;
            }
        });
    }

    public boolean isBlocked(String host) {
        if (host == null) return false;
        String lower = host.toLowerCase();
        Set<String> current = entries.get();
        if (current.contains(lower)) return true;
        return current.stream().anyMatch(e -> lower.endsWith("." + e));
    }
}