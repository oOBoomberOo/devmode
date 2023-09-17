package me.boomber.devmode.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(ServerFunctionLibrary.class)
public interface ServerFunctionLibraryMixin {
    @Accessor
    TagLoader<CommandFunction> getTagsLoader();

    @Accessor
    CommandDispatcher<CommandSourceStack> getDispatcher();

    @Accessor
    int getFunctionCompilationLevel();

    @Accessor
    void setFunctions(Map<ResourceLocation, CommandFunction> functions);

    @Accessor
    void setTags(Map<ResourceLocation, Collection<CommandFunction>> tags);

    @Invoker("readLines")
    static List<String> readLines(Resource resource) {
        throw new AssertionError();
    }
}
