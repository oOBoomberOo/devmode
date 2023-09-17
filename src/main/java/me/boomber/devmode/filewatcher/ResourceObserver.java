package me.boomber.devmode.filewatcher;

import java.util.List;

@FunctionalInterface
public interface ResourceObserver {
    void onResourceChanged(List<ResourceEvent> events);
}
