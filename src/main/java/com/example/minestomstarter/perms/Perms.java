package com.example.minestomstarter.perms;

import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class Perms {
    private static final PermissionStore store = new PermissionStore(); // per-player
    private static final PermissionCategoriesStore categories = new PermissionCategoriesStore();
    private static final PermissionRanksStore ranks = new PermissionRanksStore();

    public static void load() {
        categories.load();
        ranks.load();
        store.load();
    }

    public static PermissionCategoriesStore categories() {
        return categories;
    }

    public static boolean has(CommandSender sender, String perm) {
        if (!(sender instanceof Player p)) return true;
        return resolve(p.getUuid()).contains(perm.toLowerCase(Locale.ROOT));
    }

    public static void grant(Player p, String perm) {
        store.grant(p.getUuid(), perm);
    }

    public static void revoke(Player p, String perm) {
        store.revoke(p.getUuid(), perm);
    }

    public static void clear(Player p) {
        store.clear(p.getUuid());
    }

    public static Set<String> list(Player p) {
        return store.list(p.getUuid());
    }

    public static Set<String> all() {
        return store.all();
    }

    private static Set<String> resolve(@NotNull UUID uuid) {
        Set<String> effective = new HashSet<>();

        for (String node : store.list(uuid)) {
            if (categories.isCategory(node)) {
                effective.addAll(categories.getChildren(node));
            } else {
                effective.add(node);
            }
        }

        // TODO: hook in ranks here if you add per-player rank storage

        return effective;
    }

    public static boolean isCategory(String key) {
        return categories().isCategory(key);
    }

    public static boolean isNode(String key) {
        String k = key.toLowerCase(Locale.ROOT);
        return categories().categories.values().stream().anyMatch(list -> list.contains(k));
    }

    public static boolean isKnownPermission(String key) {
        return isCategory(key) || isNode(key);
    }

    // Handy, if you need a full list (for messages or suggestions)
    public static Set<String> allFromCategories() {
        java.util.Set<String> out = new java.util.HashSet<>();
        out.addAll(categories().categories.keySet());                  // categories
        categories().categories.values().forEach(out::addAll);         // nodes
        return out;
    }
}
