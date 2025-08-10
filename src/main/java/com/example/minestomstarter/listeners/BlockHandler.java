package com.example.minestomstarter.listeners;

import com.example.minestomstarter.perms.Perms;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.MinecraftServer;

public class BlockHandler {

    // Permission category and nodes
    private static final String BLOCK_CATEGORY = "block";
    private static final String BLOCK_BREAK = "block.break";
    private static final String BLOCK_PLACE = "block.place";

    public static void register() {
        var eventHandler = MinecraftServer.getGlobalEventHandler();

        // Block breaking
        eventHandler.addListener(PlayerBlockBreakEvent.class, event -> {
            Player player = event.getPlayer();

            if (!hasBlockPermission(player, BLOCK_BREAK)) {
                event.setCancelled(true);
                player.sendMessage("§cYou do not have permission to break blocks.");
            }
        });

        // Block placing
        eventHandler.addListener(PlayerBlockPlaceEvent.class, event -> {
            Player player = event.getPlayer();

            if (!hasBlockPermission(player, BLOCK_PLACE)) {
                event.setCancelled(true);
                player.sendMessage("§cYou do not have permission to place blocks.");
            }
        });
    }

    /**
     * Checks if the player has the general block permission or the specific sub-permission.
     */
    private static boolean hasBlockPermission(Player player, String subPermission) {
        return Perms.has(player, BLOCK_CATEGORY) || Perms.has(player, subPermission);
    }
}
