package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.ConfigManager;
import dev.jaegyu.homeBase.PDBackupManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final PDBackupManager backupManager;
    private final ConfigManager configManager;


    public PlayerDeathListener(PDBackupManager backupManager, ConfigManager configManager) {
        this.backupManager = backupManager;
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!configManager.isDeathBackupEnabled()) return;
        backupManager.backup(event.getEntity(), PDBackupManager.BackupReason.DEATH);
    }
}