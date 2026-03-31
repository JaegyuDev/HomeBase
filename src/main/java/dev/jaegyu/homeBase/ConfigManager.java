package dev.jaegyu.homeBase;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final FileConfig config;

    public ConfigManager(Plugin plugin) {
        Path configPath = plugin.getDataFolder().toPath().resolve("config.toml");

        config = FileConfig.builder(configPath)
                .defaultResource("/default_config.toml")
                .autosave()
                .build();

        config.load();
    }

    // features.enchantments
    public boolean isHarvestingEnabled() {
        return config.getOrElse("features.enchantments.harvesting", true);
    }

    // features.mobs
    public boolean isCreeperFireIgniteEnabled() {
        return config.getOrElse("features.mobs.creeper_fire_ignite", true);
    }

    public boolean isCreeperGriefingDisabled() {
        return config.getOrElse("features.mobs.disable_creeper_griefing", true);
    }

    public boolean isReturnToSenderLootEnabled() {
        return config.getOrElse("features.mobs.return_to_sender_loot", true);
    }

    // features.backup
    public boolean isBackupEnabled() {
        return config.getOrElse("features.backup.enabled", true);
    }

    public boolean isDeathBackupEnabled() {
        return isBackupEnabled() && config.getOrElse("features.backup.onDeath", true);
    }

    public boolean isBootBackupEnabled() {
        return isBackupEnabled() && config.getOrElse("features.backup.onBoot", true);
    }

    // features.commands
    public boolean isHomebaseEnabled() {
        return config.getOrElse("features.commands.homebase", true);
    }

    // features.misc
    public boolean isHeartFireworkEnabled() {
        return config.getOrElse("features.misc.heart_firework", true);
    }
    
    public boolean isAnvilXPCapped() {
        return config.getOrElse("features.misc.anvil_xp_capped", true);
    }

    public void close() {
        config.close();
    }

    public List<String> getAllKeys() {
        List<String> keys = new ArrayList<>();
        flattenKeys(config.valueMap(), "", keys);
        return keys.stream().sorted().toList();
    }

    private void flattenKeys(Map<String, Object> map, String prefix, List<String> keys) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String fullKey = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof CommentedConfig nested) {
                flattenKeys(nested.valueMap(), fullKey, keys);
            } else {
                keys.add(fullKey);
            }
        }
    }

    public Object get(String key) {
        return config.get(key);
    }

    public void set(String key, Object value) {
        config.set(key, value);
    }

    public boolean contains(String key) {
        return config.contains(key);
    }
}
