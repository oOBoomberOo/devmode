package me.boomber.devmode.filewatcher;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.StandardWatchEventKinds.*;

@AllArgsConstructor
public class ResourceWatcherVisitor extends SimpleFileVisitor<Path> {
    private final WatchService watchService;

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        dir.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        return FileVisitResult.CONTINUE;
    }
}
