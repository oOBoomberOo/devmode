package me.boomber.devmode.filewatcher;

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor
public sealed class ResourceEvent permits ModifyEvent, CreateEvent, DeleteEvent {
    public final Path path;
}

final class ModifyEvent extends ResourceEvent {
    ModifyEvent(Path path) {
        super(path);
    }

    @Override
    public String toString() {
        return "Modify[%s]".formatted(path);
    }
}

final class CreateEvent extends ResourceEvent {
    CreateEvent(Path path) {
        super(path);
    }

    @Override
    public String toString() {
        return "Create[%s]".formatted(path);
    }
}

final class DeleteEvent extends ResourceEvent {
    DeleteEvent(Path path) {
        super(path);
    }

    @Override
    public String toString() {
        return "Delete[%s]".formatted(path);
    }
}