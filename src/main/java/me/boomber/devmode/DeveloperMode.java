package me.boomber.devmode;

import me.boomber.devmode.command.DevCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class DeveloperMode implements ModInitializer {
    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(ServerBroadcaster.INSTANCE);
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, env) -> {
            dispatcher.register(DevCommand.build());
        });
    }
}
