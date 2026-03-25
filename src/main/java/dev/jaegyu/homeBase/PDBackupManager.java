package dev.jaegyu.homeBase;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PDBackupManager {

    public enum BackupReason {
        DEATH,
        MANUAL,
        SCHEDULED
    }

    private final Plugin plugin;
    private final Path backupDir;
    public final Map<UUID, Path> pendingRestores = new HashMap<>();


    public PDBackupManager(Plugin plugin) {
        this.plugin = plugin;
        this.backupDir = plugin.getDataFolder().toPath().resolve("player_backups");
        try {
            Files.createDirectories(backupDir);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to create player_backups directory: " + e.getMessage());
        }
    }

    public void backup(Player player, BackupReason reason) {
        player.saveData();

        Path playerDat = getPlayerDatPath(player.getUniqueId());

        if (!Files.exists(playerDat)) {
            plugin.getLogger().severe("Could not find .dat for " + player.getName() + " after saveData().");
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String backupName = String.format("%s_%s_%s_%s.dat",
                player.getName(),
                player.getUniqueId(),
                reason.name().toLowerCase(),
                timestamp);

        Path destination = backupDir.resolve(backupName);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Files.copy(playerDat, destination, StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("[Backup] Saved: " + backupName);
            } catch (IOException e) {
                plugin.getLogger().severe("[Backup] Failed for " + player.getName() + ": " + e.getMessage());
            }
        });
    }

    /**
     * Restores a player's data from a specific backup file.
     * Player should be offline or will be kicked to force a data reload.
     */
    public void restore(Player player, Path backupFile) {
        restore(player.getUniqueId(), backupFile);
    }

    public void restore(UUID uuid, Path backupFile) {
        pendingRestores.put(uuid, backupFile);

        Player player = plugin.getServer().getPlayer(uuid);
        if (player == null) return;

        player.kickPlayer("Your data is being restored. Please rejoin.");
    }

    public void completeRestore(UUID uuid) {
        Path backupFile = pendingRestores.remove(uuid);
        if (backupFile == null) return;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Path playerDat = getPlayerDatPath(uuid);
            try {
                Files.copy(backupFile, playerDat, StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("[Backup] Restore completed for " + uuid);
            } catch (IOException e) {
                plugin.getLogger().severe("[Backup] Restore failed for " + uuid + ": " + e.getMessage());
            }
        }, 40L); // 2-second delay (40 ticks)
    }

    /**
     * Returns all backups for a given player UUID, sorted oldest to newest.
     */
    public Path[] getBackupsForPlayer(UUID uuid) throws IOException {
        try (var stream = Files.list(backupDir)) {
            return stream
                    .filter(p -> p.getFileName().toString().contains(uuid.toString()))
                    .sorted()
                    .toArray(Path[]::new);
        }
    }


    private Path getPlayerDatPath(UUID uuid) {
        Path worldFolder = Bukkit.getWorlds().get(0).getWorldFolder().toPath();
        return worldFolder.resolve("playerdata").resolve(uuid + ".dat");
    }
}
