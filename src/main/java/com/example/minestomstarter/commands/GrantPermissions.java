package com.example.minestomstarter.commands;

import com.example.minestomstarter.perms.Perms;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.Locale;

public class GrantPermissions extends Command {
    public GrantPermissions() {
        super("grantpermissions", "perms");

        setCondition((sender, cmd) -> Perms.has(sender, "op"));

        // Arguments
        var actionArg = ArgumentType.Word("action"); // grant, revoke, clear, list
        actionArg.setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("grant"));
            suggestion.addEntry(new SuggestionEntry("revoke"));
            suggestion.addEntry(new SuggestionEntry("clear"));
            suggestion.addEntry(new SuggestionEntry("list"));
        });

        var playerArg = ArgumentType.Word("player");
        playerArg.setSuggestionCallback((sender, context, suggestion) -> {
            MinecraftServer.getConnectionManager().getOnlinePlayers()
                    .forEach(p -> suggestion.addEntry(new SuggestionEntry(p.getUsername())));
        });

        var permArg   = ArgumentType.Word("permission");
        permArg.setSuggestionCallback((sender, context, suggestion) -> {
            // suggest category names first (e.g., "block", "admin", "combat")
            Perms.categories().categories.keySet()
                    .forEach(cat -> suggestion.addEntry(new SuggestionEntry(cat)));

            // then suggest all child nodes inside each category (e.g., "block.break", "block.place")
            Perms.categories().categories.values()
                    .forEach(list -> list.forEach(node -> suggestion.addEntry(new SuggestionEntry(node))));
        });

        // /grantpermissions grant <player> <permission>
        addSyntax((sender, ctx) -> {
            String action = ctx.get(actionArg).toLowerCase(Locale.ROOT);
            String playerName = ctx.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName);

            if (target == null) {
                sender.sendMessage("Player not found: " + playerName);
                return;
            }

            String permission = ctx.get(permArg).toLowerCase(Locale.ROOT);
            if (action.equals("grant")) {
                Perms.grant(target, permission);
                refreshCommandsFor(target);
                sender.sendMessage("Granted \"" + permission + "\" to " + target.getUsername());
            } else if (action.equals("revoke")) {
                Perms.revoke(target, permission);
                refreshCommandsFor(target);
                sender.sendMessage("Revoked \"" + permission + "\" from " + target.getUsername());
            }
        }, actionArg, playerArg, permArg);

        // clear & list -> no permission arg
        addSyntax((sender, ctx) -> {
            String action = ctx.get(actionArg).toLowerCase(Locale.ROOT);
            String playerName = ctx.get(playerArg);
            Player target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(playerName);

            if (target == null) {
                sender.sendMessage("Player not found: " + playerName);
                return;
            }

            if (action.equals("clear")) {
                Perms.clear(target);
                refreshCommandsFor(target);
                sender.sendMessage("Cleared all permissions for " + target.getUsername());
            } else if (action.equals("list")) {
                var list = Perms.list(target);
                sender.sendMessage(target.getUsername() + " perms: " +
                        (list.isEmpty() ? "(none)" : String.join(", ", list)));
            }
        }, actionArg, playerArg);

        // Default executor (no args) - quick self-grant for sethealth
        setDefaultExecutor((sender, ctx) -> {
            sender.sendMessage(Component.text("Usage: /grant <action> <player> <permission>"));
        });
    }

    private void refreshCommandsFor(Player p) {
        try {
            p.refreshCommands();
        } catch (NoSuchMethodError e) {
            try {
                p.refreshCommands();
            } catch (Throwable ignored) { }
        }
    }
}
