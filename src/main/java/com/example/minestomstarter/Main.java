package com.example.minestomstarter;

import com.example.minestomstarter.commands.*;
import com.example.minestomstarter.listeners.BlockHandler;
import com.example.minestomstarter.perms.Perms;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.anvil.AnvilLoader;
import net.minestom.server.instance.block.Block;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Main {

    public static void main(String[] args) {

        MinecraftServer minecraftServer = MinecraftServer.init();

        // ---- Instance setup (persistent world in ./world) ----
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instance = instanceManager.createInstanceContainer(new AnvilLoader("world"));

        // Optional: vanilla lighting chunks (fine even for a void world)
        instance.setChunkSupplier(LightingChunk::new);

        // Void generator + small spawn platform
        instance.setGenerator(u -> {});
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                instance.setBlock(new Pos(x, 40, z), Block.SMOOTH_STONE);
            }
        }

        // ---- Preload some chunks around spawn (reduce if startup is slow) ----
        var futures = new ArrayList<CompletableFuture<Chunk>>();
        net.minestom.server.coordinate.ChunkRange.chunksInRange(0, 0, 12,
                (x, z) -> futures.add(instance.loadChunk(x, z)));
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // ---- Autosave every 60s + /save-all + shutdown flush ----
        MinecraftServer.getSchedulerManager()
                .buildTask(() -> instance.saveChunksToStorage().join())
                .repeat(Duration.ofSeconds(60)).schedule();

        Command saveAll = new Command("save-all");
        saveAll.setDefaultExecutor((sender, ctx) -> {
            sender.sendMessage("Saving world...");
            instance.saveChunksToStorage().thenRun(() -> sender.sendMessage("World saved."));
        });
        MinecraftServer.getCommandManager().register(saveAll);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { instance.saveChunksToStorage().join(); } catch (Throwable ignored) {}
        }));

        // ---- Player spawn ----
        GlobalEventHandler events = MinecraftServer.getGlobalEventHandler();
        events.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(instance);
            player.setRespawnPoint(new Pos(0, 42, 0));
        });


        Perms.load();
        MinecraftServer.getCommandManager().register(new GrantPermissions());
        MinecraftServer.getCommandManager().register(new PingCommand());
        MinecraftServer.getCommandManager().register(new SetHealthCommand());
        MinecraftServer.getCommandManager().register(new GamemodeCommand());
        MinecraftServer.getCommandManager().register(new PermsReloadCommand());



        // ---- Start server ----
        minecraftServer.start("0.0.0.0", 25565);
        BlockHandler.register();

        startConsoleInput();
    }


    private static void startConsoleInput() {
        var cm = MinecraftServer.getCommandManager();
        CommandSender console = cm.getConsoleSender();

        Thread t = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                try {
                    if (!scanner.hasNextLine()) break;
                    String line = scanner.nextLine().trim();
                    if (line.isEmpty()) continue;

                    // allow both "command" and "/command"
                    if (line.startsWith("/")) line = line.substring(1);

                    // simple builtin: stop/exit to shut down server cleanly
                    if (line.equalsIgnoreCase("stop") || line.equalsIgnoreCase("exit")) {
                        console.sendMessage("Shutting down...");
                        MinecraftServer.stopCleanly();
                        break;
                    }

                    cm.execute(console, line);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }, "ConsoleInput");
        t.setDaemon(true);
        t.start();
    }
}
