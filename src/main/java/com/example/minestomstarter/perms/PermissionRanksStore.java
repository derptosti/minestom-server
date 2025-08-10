package com.example.minestomstarter.perms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionRanksStore {
    private static final Path FILE = Path.of("ranks.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Map<String, List<String>> ranks = new HashMap<>();

    public void load() {
        try {
            if (Files.exists(FILE)) {
                try (FileReader reader = new FileReader(FILE.toFile())) {
                    PermissionRanksStore loaded = gson.fromJson(reader, PermissionRanksStore.class);
                    if (loaded != null && loaded.ranks != null) {
                        ranks = loaded.ranks;
                    }
                }
            } else {
                ranks = new HashMap<>();
                save();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ranks = new HashMap<>();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(FILE.toFile())) {
            gson.toJson(this, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
