package me.boomber.devmode.filewatcher;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import static java.nio.file.StandardWatchEventKinds.*;

public class ResourceEventFactory {
    @Nullable ResourceEvent create(WatchEvent<Path> event) {
        var kind = event.kind();
        var path = event.context();

        // this event also triggered when directory's children gets created or deleted
        if (kind.equals(ENTRY_MODIFY) && Files.isRegularFile(path)) {
            return new ModifyEvent(path);
        }

        if (kind.equals(ENTRY_CREATE)) {
            return new CreateEvent(path);
        }

        if (kind.equals(ENTRY_DELETE)) {
            return new DeleteEvent(path);
        }

        return null;
    }
}
