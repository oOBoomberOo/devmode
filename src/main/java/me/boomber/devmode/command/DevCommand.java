package me.boomber.devmode.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import lombok.experimental.UtilityClass;
import me.boomber.devmode.server.DevMode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.commands.Commands.literal;

@UtilityClass
public class DevCommand {
    private final Logger LOGGER = LoggerFactory.getLogger("DevMode");
    private final DevMode devMode = new DevMode();

    public LiteralArgumentBuilder<CommandSourceStack> build() {
        return literal("devmode")
                .executes(DevCommand::toggle)
                .then(literal("on").executes(DevCommand::enable))
                .then(literal("off").executes(DevCommand::disable));
    }

    private int toggle(CommandContext<CommandSourceStack> ctx) {
        if (devMode.isDisabled()) {
            return enable(ctx);
        } else {
            return disable(ctx);
        }
    }

    private int disable(CommandContext<CommandSourceStack> ctx) {
        devMode.disable();
        ctx.getSource().sendSuccess(() -> Component.literal("Disabled DevMode"), false);
        return 1;
    }

    private int enable(CommandContext<CommandSourceStack> ctx) {
        try {
            var source = ctx.getSource();
            var server = source.getServer();

            devMode.enable(server, 1000);

            source.sendSuccess(() -> Component.literal("Enabled DevMode"), false);

            return 1;
        } catch (Exception e) {
            LOGGER.error("Failed to setup DevMode", e);
            ctx.getSource().sendFailure(Component.literal("Failed to setup DevMode"));
            devMode.disable();
            return 0;
        }
    }
}
