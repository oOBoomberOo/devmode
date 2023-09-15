package me.boomber.devmode.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.netty.util.Timeout;
import me.boomber.devmode.ServerBroadcaster;
import me.boomber.devmode.mixin.PathPackResourcesMixin;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.nio.file.StandardWatchEventKinds.*;

public class DevCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger("DevMode");

    private static boolean isListening = false;
    private static Timer eventLoop = new Timer(true);

    public static void teardownDevMode() {
        eventLoop.cancel();
    }

    public static void setupDevMode(ResourceManager resourceManager, Consumer<WatchService> onChanged) throws IOException {
        teardownDevMode();

        var fileSystem = FileSystems.getDefault();
        var watcher = fileSystem.newWatchService();

        watchPackResources(resourceManager, watcher);

        var timer = new Timer(true);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                WatchKey finalWatchKey = null;

                while (true) {
                    var watchKey = watcher.poll();

                    // no changes in the last 500ms
                    if (finalWatchKey == null && watchKey == null) {
                        return;
                    }
                    // finished iterating through all changes
                    else if (watchKey == null) {
                        break;
                    }
                    // there are more changes to iterate through
                    else {
                        var events = watchKey.pollEvents().stream().map(it -> String.format("<%s | %s>", it.kind(), it.context()));
                        LOGGER.info("Change detected: {}", events);

                        watchKey.reset();
                        finalWatchKey = watchKey;
                    }
                }

                try {
                    watchPackResources(resourceManager, watcher);
                } catch (IOException e) {
                    LOGGER.error("Failed to watch data packs", e);
                } finally {
                    onChanged.accept(watcher);
                }
            }
        }, 100, 1000);

        eventLoop = timer;
    }

    private static void watchPackResources(ResourceManager resourceManager, WatchService watcher) throws IOException {
        var packs = resourceManager.listPacks().filter(it -> !it.isBuiltin()).toList();

        for (PackResources pack : packs) {
            if (pack instanceof PathPackResources p) {
                var root = ((PathPackResourcesMixin) pack).getRoot();
                watch(root, watcher);
            }
        }
    }

    private static void watch(Path root, WatchService watcher) throws IOException {
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                LOGGER.info("Watching {}", dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.literal("devmode");
        LiteralArgumentBuilder<CommandSourceStack> on = LiteralArgumentBuilder.literal("on");
        LiteralArgumentBuilder<CommandSourceStack> off = LiteralArgumentBuilder.literal("off");

        return root.executes(DevCommand::toggle)
                .then(on.executes(DevCommand::enable))
                .then(off.executes(DevCommand::disable));
    }

    private static int toggle(CommandContext<CommandSourceStack> ctx) {
        if (isListening) {
            return disable(ctx);
        } else {
            return enable(ctx);
        }
    }

    private static int disable(CommandContext<CommandSourceStack> ctx) {
        isListening = false;
        ctx.getSource().sendSuccess(() -> Component.literal("Disabled DevMode"), false);

        teardownDevMode();

        return 1;
    }

    private static int enable(CommandContext<CommandSourceStack> ctx) {
        try {
            isListening = true;

            var source = ctx.getSource();

            var server = source.getServer();
            var resourceManager = server.getResourceManager();

            setupDevMode(resourceManager, watcher -> {
                var serverSource = server.createCommandSourceStack();
                server.execute(() -> {
                    ServerBroadcaster.clearChat();
                    server.getCommands().performPrefixedCommand(serverSource, "reload");
                });
            });

            source.sendSuccess(() -> Component.literal("Enabled DevMode"), false);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Failed to setup DevMode", e);
            ctx.getSource().sendFailure(Component.literal("Failed to setup DevMode"));
            return 0;
        }
    }
}
