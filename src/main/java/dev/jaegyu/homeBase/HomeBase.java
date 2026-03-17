package dev.jaegyu.homeBase;

import dev.jaegyu.homeBase.commands.HbCommand;
import dev.jaegyu.homeBase.commands.HbTabCompleter;
import dev.jaegyu.homeBase.commands.HomeCommand;
import dev.jaegyu.homeBase.enchantments.HarvestingEnchant;
import dev.jaegyu.homeBase.listener.*;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class HomeBase extends JavaPlugin {

    private final ConfigManager configManager = new ConfigManager(this);
    private final PDBackupManager backupManager = new PDBackupManager(this);

    @Override
    public void onEnable() {
        HomeCommand homeCommand = new HomeCommand(this, configManager);

        getCommand("home").setExecutor(homeCommand);

        var hbCommand = getCommand("hb");
        hbCommand.setExecutor(new HbCommand(configManager, backupManager));
        hbCommand.setTabCompleter(new HbTabCompleter(configManager, backupManager));

        HarvestingEnchant.init(this);

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


    public void log(String msg) {
        this.getLogger().info(msg);
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
