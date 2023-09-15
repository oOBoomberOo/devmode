package me.boomber.devmode.mixin;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandFunction;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.FileToIdConverter;
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
    @Accessor("tagsLoader")
    TagLoader<CommandFunction> getTagsLoader();

    @Accessor("dispatcher")
    CommandDispatcher<CommandSourceStack> getDispatcher();

    @Accessor("functionCompilationLevel")
    int getFunctionCompilationLevel();

    @Accessor("functions")
    void setFunctions(Map<ResourceLocation, CommandFunction> functions);

    @Accessor("tags")
    void setTags(Map<ResourceLocation, Collection<CommandFunction>> tags);

    @Accessor("LISTER")
    static FileToIdConverter getLister() {
        throw new AssertionError();
    }

    @Invoker("readLines")
    static List<String> readLines(Resource resource) {
        throw new AssertionError();
    }
}
