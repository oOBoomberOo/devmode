package me.boomber.devmode.mixin;

import me.boomber.devmode.DevServerFunctionLibrary;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @Mutable
    @Shadow @Final private ServerFunctionLibrary functionLibrary;

    @Shadow @Final private Commands commands;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(RegistryAccess.Frozen frozen, FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection, int i, CallbackInfo ci) {
        this.functionLibrary = new DevServerFunctionLibrary(i, commands.getDispatcher());
    }
}
