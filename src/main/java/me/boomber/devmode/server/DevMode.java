package me.boomber.devmode.server;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import me.boomber.devmode.filewatcher.NoopWatcher;
import me.boomber.devmode.filewatcher.PeriodicResourceWatcher;
import me.boomber.devmode.filewatcher.ResourceWatcher;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

@RequiredArgsConstructor
@Data
public class DevMode {
    private ResourceWatcher watcher = new NoopWatcher();
    private MinecraftServer server;

    public void setWatcher(ResourceWatcher watcher) {
        this.watcher.stop();
        this.watcher = watcher;
    }

    public boolean isDisabled() {
        return watcher instanceof NoopWatcher;
    }

    public void register(ResourceManager resourceManager) {
        resourceManager.listPacks().filter(it -> !it.isBuiltin()).forEach(packResources -> {
            if (packResources instanceof PathPackResources pack) {
                watcher.register(pack);
            }
        });
    }

    void reload() {
        var source = server.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(source, "reload");
        DevModeFeedback.INSTANCE.clear();
    }

    public void enable(MinecraftServer server, long period) throws IOException {
        var watcher = new PeriodicResourceWatcher(period);
        watcher.watch((events) -> reload());
        setServer(server);
        setWatcher(watcher);
        register(server.getResourceManager());
    }

    public void disable() {
        setWatcher(new NoopWatcher());
    }
}
