package dev.jaegyu.homeBase.commands;

import dev.jaegyu.homeBase.ConfigManager;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HomeCommand implements BasicCommand {

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
    public void execute(CommandSourceStack stack, String[] args) {
        if (!configManager.isHomebaseEnabled()) {
            stack.getSender().sendMessage("The home command is currently disabled.");
            return;
        }

        if (!(stack.getSender() instanceof Player player)) {
            stack.getSender().sendMessage("You cannot call this command as console!");
            return;
        }

        Location respawn = player.getRespawnLocation();
        if (respawn == null) {
            player.sendMessage("Cannot obtain your respawn location :(!");
            player.sendMessage("Make sure you've slept in a bed or used a respawn anchor!");
            return;
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
                player.sendMessage("Failed to teleport you? :/");
            }
        }, 60L); // 3 seconds

        pendingTeleports.put(uuid, task);
    }

    public Map<UUID, BukkitTask> getPendingTeleports() {
        return pendingTeleports;
    }
}