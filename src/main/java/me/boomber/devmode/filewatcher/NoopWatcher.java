package me.boomber.devmode.filewatcher;

import net.minecraft.server.packs.PathPackResources;

public class NoopWatcher implements ResourceWatcher {
    @Override
    public void register(PathPackResources packResources) {

    }

    @Override
    public void watch(ResourceObserver observer) {

    }

    @Override
    public void stop() {

    }
}
