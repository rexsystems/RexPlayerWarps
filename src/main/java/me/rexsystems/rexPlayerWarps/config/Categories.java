package me.rexsystems.rexPlayerWarps.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Categories {

    private final Map<String, Category> categories = new LinkedHashMap<>();

    public void load(File file) {
        categories.clear();
        if (file == null || !file.exists()) {
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("categories");
        if (section == null) {
            return;
        }
        for (String id : section.getKeys(false)) {
            ConfigurationSection child = section.getConfigurationSection(id);
            if (child == null) {
                continue;
            }
            String key = id.toLowerCase(Locale.ROOT);
            categories.put(key, new Category(key, child.getString("display", id), child.getString("icon", "PAPER")));
        }
    }

    public boolean exists(String id) {
        return id != null && categories.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public String display(String id) {
        Category category = id == null ? null : categories.get(id.toLowerCase(Locale.ROOT));
        return category == null ? id == null ? "" : id : category.display;
    }

    public String icon(String id) {
        Category category = id == null ? null : categories.get(id.toLowerCase(Locale.ROOT));
        return category == null ? "PAPER" : category.icon;
    }

    public List<String> ids() {
        return new ArrayList<>(categories.keySet());
    }

    public void saveDefault(File file, String resourceCopy) throws IOException {
        if (file.exists()) {
            return;
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create " + parent.getAbsolutePath());
        }
    }

    public record Category(String id, String display, String icon) {
    }
}
