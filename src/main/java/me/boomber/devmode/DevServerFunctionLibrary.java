package me.boomber.devmode;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import me.boomber.devmode.data.ParsingError;
import me.boomber.devmode.data.ParsingFailure;
import me.boomber.devmode.mixin.ServerFunctionLibraryMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public class DevServerFunctionLibrary extends ServerFunctionLibrary {
    private static final Logger LOGGER = LogUtils.getLogger();

    public DevServerFunctionLibrary(int level, CommandDispatcher<CommandSourceStack> commandDispatcher) {
        super(level, commandDispatcher);
    }

    public void broadcastErrors(List<ParsingError> errors) {
        for (ParsingError error : errors) {
            var content = error.parsingFailure.formatMessage();
            ServerBroadcaster.send(content);
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier,
                                                   ResourceManager resourceManager,
                                                   ProfilerFiller profilerFiller,
                                                   ProfilerFiller profilerFiller2,
                                                   Executor executor,
                                                   Executor executor2) {
        var internal = (ServerFunctionLibraryMixin) this;

        var f1 = CompletableFuture.supplyAsync(() -> internal.getTagsLoader().load(resourceManager), executor);
        var f2 = CompletableFuture.supplyAsync(() -> ServerFunctionLibraryMixin.getLister().listMatchingResources(resourceManager), executor).thenCompose((resourceMap) -> {
            var commandSourceStack = new CommandSourceStack(CommandSource.NULL,
                    Vec3.ZERO,
                    Vec2.ZERO,
                    null,
                    internal.getFunctionCompilationLevel(),
                    "",
                    CommonComponents.EMPTY,
                    null,
                    null);

            @SuppressWarnings("unchecked")
            CompletableFuture<ParseFunction>[] tasks = resourceMap.entrySet().stream().map((entry) -> {
                var location = entry.getKey();
                var resource = entry.getValue();
                return parseFunction(location, resource, executor, internal.getDispatcher(), commandSourceStack);
            }).toArray(CompletableFuture[]::new);

            return CompletableFuture.allOf(tasks).handle((void_, throwable) -> tasks);
        });

        return f1.thenCombine(f2, Pair::of).thenCompose(preparationBarrier::wait).thenAcceptAsync(pair -> {
            var tags = pair.getFirst();
            var functions = pair.getSecond();
            ImmutableMap.Builder<ResourceLocation, CommandFunction> builder = ImmutableMap.builder();
            var errors = new java.util.ArrayList<ParsingError>();

            for (var function : functions) {
                function.handle((parseFunction, throwable) -> {

                    if (throwable instanceof CompletionException exception) {
                        handleError(exception, errors);
                    } else {
                        builder.put(parseFunction.resourceLocation, parseFunction.function);
                    }

                    return null;
                }).join();
            }

            broadcastErrors(errors);

            internal.setFunctions(builder.build());
            internal.setTags(internal.getTagsLoader().build(tags));
        }, executor2);
    }

    private void handleError(CompletionException exception, List<ParsingError> errors) {
        if (exception.getCause() instanceof ParsingError parsingError) {
            var location = parsingError.resourceLocation;
            errors.add(parsingError);
            LOGGER.error("Failed to load function {}", location, exception);
        } else {
            LOGGER.error("Failed to load function", exception);
        }
    }

    private CompletableFuture<ParseFunction> parseFunction(ResourceLocation location, Resource resource, Executor executor, CommandDispatcher<CommandSourceStack> dispatcher, CommandSourceStack commandSourceStack) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> sourceCode = ServerFunctionLibraryMixin.readLines(resource);

            try {
                var id = ServerFunctionLibraryMixin.getLister().fileToId(location);
                var result = CommandFunction.fromLines(id, dispatcher, commandSourceStack, sourceCode);
                return new ParseFunction(result, id);
            } catch (IllegalArgumentException error) {
                throw new ParsingError(error, ParsingFailure.from(location, error, sourceCode), location);
            }
        }, executor);
    }

    private record ParseFunction(CommandFunction function, ResourceLocation resourceLocation) {}
}
