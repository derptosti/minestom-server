package com.example.minestomstarter.commands;

import com.example.minestomstarter.perms.Perms;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;

public class PermsReloadCommand extends Command {

    public PermsReloadCommand() {
        super("permsreload");

        // Only allow ops to reload permissions
        setCondition((sender, cmd) -> Perms.has(sender, "op"));

        setDefaultExecutor((sender, ctx) -> {
            try {
                Perms.load(); // Reload permissions from JSON
                sender.sendMessage("Permissions reloaded from file.");

                // Optionally refresh commands for all online players
                for (Player p : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
                    try {
                        p.refreshCommands();
                    } catch (NoSuchMethodError ignored) {}
                }

            } catch (Exception e) {
                sender.sendMessage("Failed to reload permissions: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
