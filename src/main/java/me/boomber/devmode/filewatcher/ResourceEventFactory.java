package me.boomber.devmode.filewatcher;

import org.jetbrains.annotations.Nullable;

import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class ResourceEventFactory {
    private final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.mcfunction");

    @Nullable ResourceEvent create(WatchEvent<Path> event) {

        var kind = event.kind();
        var path = event.context();

        // this event also triggered when directory's children gets created or deleted
        if (kind == ENTRY_MODIFY && matcher.matches(path)) {
            return new ModifyEvent(path);
        }

        if (kind == ENTRY_CREATE) {
            return new CreateEvent(path);
        }

        if (kind == ENTRY_DELETE) {
            return new DeleteEvent(path);
        }

        return null;
    }
}
