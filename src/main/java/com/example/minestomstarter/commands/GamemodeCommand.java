package com.example.minestomstarter.commands;

import com.example.minestomstarter.perms.Perms;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

import java.util.Locale;

import static net.kyori.adventure.text.Component.text;

public class GamemodeCommand extends Command {

    public GamemodeCommand() {
        super("gamemode", "gm");

        var modeArg = ArgumentType.Word("mode"); // we'll parse this manually
        var playerArg = ArgumentType.Word("player");

        // Player name tab suggestions
        playerArg.setSuggestionCallback((sender, ctx, sug) ->
                MinecraftServer.getConnectionManager().getOnlinePlayers()
                        .forEach(p -> sug.addEntry(new SuggestionEntry(p.getUsername())))
        );

        // Gamemode name tab suggestions
        modeArg.setSuggestionCallback((sender, ctx, sug) -> {
            for (GameMode gm : GameMode.values()) {
                sug.addEntry(new SuggestionEntry(gm.name().toLowerCase(Locale.ROOT)));
            }
        });

        // /gamemode <mode> [player]
        addSyntax((sender, ctx) -> {
            GameMode mode;
            try {
                mode = GameMode.valueOf(ctx.get(modeArg).toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Unknown gamemode: " + ctx.get(modeArg));
                return;
            }

            Player target;
            if (ctx.getRaw(playerArg) != null) {
                target = MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(ctx.get(playerArg));
                if (target == null) {
                    sender.sendMessage("Player not found: " + ctx.get(playerArg));
                    return;
                }
            } else {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Console must specify a player: /gamemode <mode> <player>");
                    return;
                }
                target = (Player) sender;
            }

            if (!hasGamemodePermission(sender, mode)) {
                sender.sendMessage("You don't have permission for this gamemode!");
                return;
            }

            target.setGameMode(mode);
            if (sender == target) {
                target.sendMessage(text("Gamemode changed to " + mode.name()));
            } else {
                sender.sendMessage("Changed gamemode of " + target.getUsername() + " to " + mode.name());

                target.sendMessage(
                        text("Your gamemode has been changed to ")
                                .append(text(mode.name()).color(NamedTextColor.AQUA))
                                .append(text(" by "))
                                .append(text(senderName(sender)).color(NamedTextColor.YELLOW))
                );
            }
        }, modeArg, playerArg);

        // /gamemode <mode> (self only)
        addSyntax((sender, ctx) -> {
            GameMode mode;
            try {
                mode = GameMode.valueOf(ctx.get(modeArg).toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                sender.sendMessage("Unknown gamemode: " + ctx.get(modeArg));
                return;
            }

            if (!(sender instanceof Player target)) {
                sender.sendMessage("Console must specify a player: /gamemode <mode> <player>");
                return;
            }

            if (!hasGamemodePermission(sender, mode)) {
                sender.sendMessage("You don't have permission for this gamemode!");
                return;
            }

            target.setGameMode(mode);
            target.sendMessage(text("Gamemode changed to " + mode.name().toLowerCase()));
        }, modeArg);
    }

    private boolean hasGamemodePermission(CommandSender sender, GameMode mode) {
        String gmPerm = "gamemode." + mode.name().toLowerCase(Locale.ROOT);
        return Perms.has(sender, "op")
                || Perms.has(sender, "gamemode")
                || Perms.has(sender, gmPerm);
    }

    private String senderName(CommandSender sender) {
        return (sender instanceof Player p) ? p.getUsername() : "Console";
    }
}
