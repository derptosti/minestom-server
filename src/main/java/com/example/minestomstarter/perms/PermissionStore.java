package com.example.minestomstarter.perms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PermissionStore {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = Path.of("player_perms.json"); // <-- player file only!

    // in-memory: UUID -> set of direct grants
    private Map<UUID, Set<String>> perms = new HashMap<>();

    public void load() {
        System.out.println("[Perms] Loading " + FILE.toAbsolutePath());
        if (!Files.exists(FILE)) {
            perms = new HashMap<>();
            return;
        }
        try (FileReader reader = new FileReader(FILE.toFile())) {
            // Load as Map<String, Set<String>> to avoid key-type weirdness,
            // then convert to UUID and merge duplicates safely.
            Type type = new TypeToken<Map<String, Set<String>>>() {}.getType();
            Map<String, Set<String>> raw = gson.fromJson(reader, type);

            Map<UUID, Set<String>> fixed = new HashMap<>();
            if (raw != null) {
                for (var e : raw.entrySet()) {
                    try {
                        UUID id = UUID.fromString(e.getKey());
                        Set<String> vals = normalize(e.getValue());
                        fixed.merge(id, vals, (a, b) -> { a.addAll(b); return a; });
                    } catch (IllegalArgumentException badKey) {
                        // skip non-UUID keys silently
                    }
                }
            }
            perms = fixed;
            System.out.println("[Perms] Loaded " + perms.size() + " player entries.");
        } catch (IOException ex) {
            ex.printStackTrace();
            perms = new HashMap<>();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(FILE.toFile(), false)) {
            // Write back as Map<String, Set<String>> (string UUID keys)
            Map<String, Set<String>> out = new LinkedHashMap<>();
            for (var e : perms.entrySet()) {
                out.put(e.getKey().toString(), new TreeSet<>(e.getValue())); // sorted for sanity
            }
            gson.toJson(out, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void grant(UUID uuid, String perm) {
        perms.computeIfAbsent(uuid, k -> new HashSet<>())
                .add(perm.toLowerCase(Locale.ROOT));
        save();
    }

    public boolean has(UUID uuid, String perm) {
        return perms.getOrDefault(uuid, Set.of())
                .contains(perm.toLowerCase(Locale.ROOT));
    }

    public void revoke(UUID uuid, String perm) {
        Set<String> set = perms.get(uuid);
        if (set != null) {
            set.remove(perm.toLowerCase(Locale.ROOT));
            if (set.isEmpty()) perms.remove(uuid);
            save();
        }
    }

    public void clear(UUID uuid) {
        if (perms.remove(uuid) != null) save();
    }

    public Set<String> list(UUID uuid) {
        return new HashSet<>(perms.getOrDefault(uuid, Set.of()));
    }

    // Returns the union of all grants across players (useful for suggestions if you want it)
    public Set<String> all() {
        Set<String> all = new HashSet<>();
        for (Set<String> s : perms.values()) all.addAll(s);
        return all;
    }

    private static Set<String> normalize(Set<String> in) {
        if (in == null) return new HashSet<>();
        Set<String> out = new HashSet<>(in.size());
        for (String s : in) if (s != null) out.add(s.toLowerCase(Locale.ROOT));
        return out;
    }
}
