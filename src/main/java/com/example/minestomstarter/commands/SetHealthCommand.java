package com.example.minestomstarter.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import com.example.minestomstarter.perms.Perms;

import static net.kyori.adventure.text.event.HoverEvent.hoverEvent;
import static net.kyori.adventure.text.event.HoverEvent.showText;

public class SetHealthCommand extends Command {
    public SetHealthCommand() {
        super("sethealth");

        //Set a condition for the command
        setCondition((sender, cmd) -> Perms.has(sender, "sethealth"));

        setDefaultExecutor((commandSender, commandContext) -> {
            commandSender.sendMessage("Usage: /sethealth <amount>");
                });

        var healthAmountArg = ArgumentType.Integer("healthAmount");

        //Command: /sethealth <amount>
        addSyntax((sender, commandContext) -> {
           Integer newHealth = commandContext.get(healthAmountArg);

           if (newHealth <= 1 || newHealth > 20) {
               sender.sendMessage("Health amount must be between 1 and 20");
               return;
           }

           if (sender instanceof Player player) {
               player.setHealth(newHealth);
               sender.sendMessage(Component.text("Health ", NamedTextColor.DARK_RED, TextDecoration.BOLD)
                       .append(Component.text("set to: ", NamedTextColor.WHITE))
                       .append(Component.text(newHealth, NamedTextColor.GREEN, TextDecoration.BOLD))
                       .append(Component.text("❤", NamedTextColor.DARK_RED).hoverEvent(Component.text(newHealth + "❤", NamedTextColor.DARK_GREEN, TextDecoration.BOLD))));
           }

        }, healthAmountArg);
    }
}
