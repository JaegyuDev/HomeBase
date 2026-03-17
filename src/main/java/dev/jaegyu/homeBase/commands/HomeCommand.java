package dev.jaegyu.homeBase.commands;

import dev.jaegyu.homeBase.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeCommand implements CommandExecutor {

    /*  This was added because I got bored walking back from little excursions.
     *  This needs abuse protection and a cost/cd.
     *
     *    - Jae, 02/24/26
     * */

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, BukkitTask> pendingTeleports = new HashMap<>();

    public HomeCommand(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!configManager.isHomebaseEnabled()) {
            sender.sendMessage("The home command is currently disabled.");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("You cannot call this command as console!");
            return false;
        }

        Location respawn = player.getRespawnLocation();
        if (respawn == null) {
            sender.sendMessage("Cannot obtain your repawn location :(!");
            sender.sendMessage("Make sure you've slept in a bed or used a respawn anchor!");
            return true;
        }

        UUID uuid = player.getUniqueId();

        BukkitTask existing = pendingTeleports.remove(uuid);
        if (existing != null) {
            existing.cancel();
        }

        player.sendMessage("Teleporting you in 3 seconds. Hang on tight!");

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            pendingTeleports.remove(uuid);

            if (!player.isOnline()) return;

            boolean success = player.teleport(respawn);
            if (!success) {
                sender.sendMessage("Failed to teleport you? :/");
            }
        }, 60L); // 3 seconds

        pendingTeleports.put(uuid, task);
        return true;
    }

    public Map<UUID, BukkitTask> getPendingTeleports() {
        return pendingTeleports;
    }
}
