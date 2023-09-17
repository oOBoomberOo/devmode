package me.boomber.devmode.filewatcher;

import net.minecraft.server.packs.PathPackResources;

/**
 * An abstraction above {@link java.nio.file.WatchService} that watch for data pack's resource for changes.
 */
public interface ResourceWatcher {
    /**
     * Register a new resource pack to be monitored.
     */
    void register(PathPackResources packResources);
    /**
     * Register an observer to be notified when any resource changes.
     */
    void watch(ResourceObserver observer);
    /**
     * Stop watching resource and drop all observers. Calling this method is effectively destroying the ResourceWatcher,
     * and does not guarantee that it can be reuse again.
     */
    void stop();
}
