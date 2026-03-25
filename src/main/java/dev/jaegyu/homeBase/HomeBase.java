package dev.jaegyu.homeBase;

import dev.jaegyu.homeBase.commands.HbCommand;
import dev.jaegyu.homeBase.commands.HomeCommand;
import dev.jaegyu.homeBase.listener.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class HomeBase extends JavaPlugin {

    private static HomeBase instance;

    private final ConfigManager configManager = new ConfigManager(this);
    private final PDBackupManager backupManager = new PDBackupManager(this);

    @Override
    public void onEnable() {
        instance = this;

        HomeCommand homeCommand = new HomeCommand(this, configManager);
        registerCommand("home", "Teleports you to your respawn location", homeCommand);
        registerCommand("hb", "HomeBase admin commands", new HbCommand(configManager, backupManager));

        registerListeners(
                new DamageListener(homeCommand, configManager),
                new PlayerDeathListener(backupManager, configManager),
                new EntityDeathListener(configManager),
                new PlayerQuitListener(backupManager),
                new VillagerTradeListener(this, configManager),
                new HarvestingListener(configManager),
                new EnchantTableListener(configManager),
                new AnvilListener(configManager),
                new ArmorStandListener(this)
        );
    }

    public static void log(String msg) {
        instance.getLogger().info(msg);
    }

    @Override
    public void onDisable() {
        configManager.close();
    }

    public void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }
}