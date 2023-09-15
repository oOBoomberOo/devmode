package me.boomber.devmode;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.Queue;

public class ServerBroadcaster implements ServerTickEvents.EndTick {
    public static ServerBroadcaster INSTANCE = new ServerBroadcaster();

    private final Queue<Component> messageQueues = new ArrayDeque<>();

    public void sendMessage(Component component) {
        messageQueues.add(component);
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        var message = messageQueues.poll();

        if (message != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.sendSystemMessage(message);
            }
            server.getPlayerList().broadcastSystemMessage(message, false);
        }
    }

    public static void send(Component component) {
        INSTANCE.sendMessage(component);
    }
    public static void clearChat() {
        var clearChat = "\n".repeat(80);
        send(Component.literal(clearChat));
    }
}
