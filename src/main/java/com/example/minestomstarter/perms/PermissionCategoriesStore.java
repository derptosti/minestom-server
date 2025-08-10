package com.example.minestomstarter.perms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionCategoriesStore {
    private static final Path FILE = Path.of("permissions.json");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Map<String, List<String>> categories = new HashMap<>();

    public void load() {
        try {
            if (Files.exists(FILE)) {
                try (FileReader reader = new FileReader(FILE.toFile())) {
                    PermissionCategoriesStore loaded = gson.fromJson(reader, PermissionCategoriesStore.class);
                    if (loaded != null && loaded.categories != null) {
                        categories = loaded.categories;
                    }
                }
            } else {
                categories = new HashMap<>();
                save();
            }
        } catch (Exception e) {
            e.printStackTrace();
            categories = new HashMap<>();
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(FILE.toFile())) {
            gson.toJson(this, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<String> getChildren(String category) {
        return categories.getOrDefault(category, Collections.emptyList());
    }

    public boolean isCategory(String name) {
        return categories.containsKey(name);
    }
}
