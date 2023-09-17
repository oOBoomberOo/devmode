package me.boomber.devmode.server;

import me.boomber.devmode.text.Text;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class DevModeFeedback implements ServerTickEvents.EndTick {
    public static DevModeFeedback INSTANCE = new DevModeFeedback();

    private final List<Component> messages = new ArrayList<>();

    public void sendMessage(Component component) {
        messages.add(component);
    }

    public void clear() {
        var text = Component.literal("\n".repeat(25));
        sendMessage(text);
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        if (messages.isEmpty()) {
            return;
        }

        var message = Text.join(messages, Text.literal("\n\n"));
        messages.clear();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(message);
        }
    }
}
