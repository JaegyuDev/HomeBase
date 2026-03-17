package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.PDBackupManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class PlayerQuitListener implements Listener {

    private final PDBackupManager backupManager;

    public PlayerQuitListener(PDBackupManager backupManager) {
        this.backupManager = backupManager;
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        backupManager.completeRestore(event.getPlayer().getUniqueId());
    }
}