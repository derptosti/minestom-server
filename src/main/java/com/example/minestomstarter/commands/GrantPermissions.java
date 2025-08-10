package com.example.minestomstarter.commands;

import com.example.minestomstarter.perms.Perms;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class GrantPermissions extends Command {
    public GrantPermissions() {
        super("perms");

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

        var permArg = ArgumentType.Word("permission");

        permArg.setSuggestionCallback((sender, ctx, sug) -> {
            String raw = ctx.getRaw(permArg);
            String q = (raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT));

            var cats = Perms.categories().categories; // Map<String, List<String>>
            Set<String> added = new HashSet<>();

            if (q.isEmpty()) {
                // No prefix → show all categories + all child perms
                for (String cat : cats.keySet()) {
                    if (added.add(cat)) sug.addEntry(new SuggestionEntry(cat));
                }
                for (var e : cats.entrySet()) {
                    for (String node : e.getValue()) {
                        if (added.add(node)) sug.addEntry(new SuggestionEntry(node));
                    }
                }
            } else {
                // Show matching categories
                for (String cat : cats.keySet()) {
                    if (cat.toLowerCase(Locale.ROOT).startsWith(q) && added.add(cat)) {
                        sug.addEntry(new SuggestionEntry(cat));
                    }
                }

                // Show matching child perms
                for (var e : cats.entrySet()) {
                    for (String node : e.getValue()) {
                        if (node.toLowerCase(Locale.ROOT).startsWith(q) && added.add(node)) {
                            sug.addEntry(new SuggestionEntry(node));
                        }
                    }
                }

                // Optional nicety: if exactly matches a category, only show its children
                if (cats.containsKey(q)) {
                    sug.getEntries().clear(); // remove other suggestions
                    added.clear();
                    if (added.add(q)) sug.addEntry(new SuggestionEntry(q));
                    for (String node : cats.get(q)) {
                        if (added.add(node)) sug.addEntry(new SuggestionEntry(node));
                    }
                }
            }
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

            if (!Perms.isKnownPermission(permission)) {
                sender.sendMessage("Unknown permission \"" + permission + "\". " +
                        "Use a category or node defined in permissions.json.");
                // Optional: show a short hint of matches
                // var sample = Perms.allFromCategories().stream().limit(8).toList();
                // sender.sendMessage("Examples: " + String.join(", ", sample));
                return;
            }

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
