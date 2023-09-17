package me.boomber.devmode.filewatcher;

import lombok.SneakyThrows;
import me.boomber.devmode.mixin.PathPackResourcesMixin;
import net.minecraft.server.packs.PathPackResources;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A {@link ResourceWatcher} that accumulate events on a fixed interval.
 */
public class PeriodicResourceWatcher implements ResourceWatcher {
    private final WatchService watchService = FileSystems.getDefault().newWatchService();
    private final ArrayList<ResourceObserver> observerList = new ArrayList<>();
    private final Timer timer = new Timer(true);

    public PeriodicResourceWatcher(long period) throws IOException {
        timer.scheduleAtFixedRate(new ResourceWatcherTask(), 0, period);
    }

    @Override
    @SneakyThrows
    public void register(PathPackResources packResources) {
        var root = ((PathPackResourcesMixin) packResources).getRoot();
        Files.walkFileTree(root, new ResourceWatcherVisitor(watchService));
    }

    @Override
    public void watch(ResourceObserver observer) {
        observerList.add(observer);
    }

    @Override
    @SneakyThrows
    public void stop() {
        timer.cancel();
        watchService.close();
    }

    private void notifyObservers(List<ResourceEvent> events) {
        for (var resourceObserver : observerList) {
            resourceObserver.onResourceChanged(events);
        }
    }

    class ResourceWatcherTask extends TimerTask {
        private final ResourceEventFactory eventFactory = new ResourceEventFactory();

        @Override
        public void run() {
            var events = new ArrayList<ResourceEvent>();

            while (true) {
                @Nullable var key = watchService.poll();

                if (key == null) break;

                events.addAll(getEvents(key));
                key.reset();
            }

            if (events.isEmpty()) return;

            notifyObservers(events);
        }

        @SuppressWarnings("unchecked")
        private List<ResourceEvent> getEvents(@NotNull WatchKey watchKey) {
            return watchKey.pollEvents()
                    .stream()
                    .map(it -> (WatchEvent<Path>) it)
                    .map(eventFactory::create)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }
}
