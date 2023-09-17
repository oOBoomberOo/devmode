package me.boomber.devmode.server;

import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import me.boomber.devmode.data.FunctionParseException;
import me.boomber.devmode.mixin.ReloadableServerResourcesMixin;
import me.boomber.devmode.mixin.ServerFunctionLibraryMixin;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
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

/**
 * A replacement for {@link ServerFunctionLibrary} that catch parsing errors and report it back to the chat.
 * It overrides {@link #reload} method so that it can report errors whenever the server is /reload.
 *
 * @apiNote
 * This class is injected into the constructor of {@link ReloadableServerResources} via {@link ReloadableServerResourcesMixin}
 */
public class DevServerFunctionLibrary extends ServerFunctionLibrary {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter LISTER = new FileToIdConverter("functions", ".mcfunction");

    public DevServerFunctionLibrary(int level, CommandDispatcher<CommandSourceStack> commandDispatcher) {
        super(level, commandDispatcher);
    }

    // Magic function, mostly copied from super but with the error collection logic
    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier,
                                                   ResourceManager resourceManager,
                                                   ProfilerFiller profilerFiller,
                                                   ProfilerFiller profilerFiller2,
                                                   Executor executor,
                                                   Executor executor2) {
        var internal = (ServerFunctionLibraryMixin) this;

        var f1 = CompletableFuture.supplyAsync(() -> internal.getTagsLoader().load(resourceManager), executor);
        var f2 = CompletableFuture.supplyAsync(() -> LISTER.listMatchingResources(resourceManager), executor).thenCompose((resourceMap) -> {
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
            CompletableFuture<ParsedFunction>[] tasks = resourceMap.entrySet().stream().map((entry) -> {
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
            var errors = new java.util.ArrayList<FunctionParseException>();

            for (var function : functions) {
                function.handle((parsedFunction, throwable) -> {

                    if (throwable instanceof CompletionException exception) {
                        handleError(exception, errors);
                    } else {
                        builder.put(parsedFunction.resourceLocation, parsedFunction.function);
                    }

                    return null;
                }).join();
            }

            broadcastErrors(errors);

            internal.setFunctions(builder.build());
            internal.setTags(internal.getTagsLoader().build(tags));
        }, executor2);
    }

    private void broadcastErrors(List<FunctionParseException> errors) {
        for (var error : errors) {
            var content = error.format();
            DevModeFeedback.INSTANCE.sendMessage(content);
        }
    }

    private void handleError(CompletionException exception, List<FunctionParseException> errors) {
        if (exception.getCause() instanceof FunctionParseException parsingError) {
            var location = parsingError.location();
            errors.add(parsingError);
            LOGGER.error("Failed to load function {}\n{}", location, parsingError.format().getString());
        } else {
            LOGGER.error("Failed to load function", exception);
        }
    }

    private CompletableFuture<ParsedFunction> parseFunction(ResourceLocation location, Resource resource, Executor executor, CommandDispatcher<CommandSourceStack> dispatcher, CommandSourceStack commandSourceStack) {
        return CompletableFuture.supplyAsync(() -> {
            List<String> sourceCode = ServerFunctionLibraryMixin.readLines(resource);

            try {
                var id = LISTER.fileToId(location);
                var result = CommandFunction.fromLines(id, dispatcher, commandSourceStack, sourceCode);
                return new ParsedFunction(result, id);
            } catch (IllegalArgumentException error) {
                throw FunctionParseException.from(location, error, sourceCode);
            }
        }, executor);
    }

    private record ParsedFunction(CommandFunction function, ResourceLocation resourceLocation) {}
}
