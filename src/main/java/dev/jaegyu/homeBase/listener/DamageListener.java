package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.ConfigManager;
import dev.jaegyu.homeBase.commands.HomeCommand;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class DamageListener implements Listener {

    private final HomeCommand homeCommand;
    private final ConfigManager configManager;

    public DamageListener(HomeCommand homeCommand, ConfigManager configManager) {
        this.homeCommand = homeCommand;
        this.configManager = configManager;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        BukkitTask task = homeCommand.getPendingTeleports().remove(uuid);

        if (task != null) {
            task.cancel();
            player.sendMessage("Teleport cancelled due to damage :(");
        }
    }

    @EventHandler
    public void onCreeperFireDamage(EntityDamageEvent event) {
        if (!configManager.isCreeperFireIgniteEnabled()) return;

        if (!(event.getEntity() instanceof Creeper creeper)) return;

        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.FIRE
                && cause != EntityDamageEvent.DamageCause.FIRE_TICK
                && cause != EntityDamageEvent.DamageCause.LAVA) return;

        if (!creeper.isIgnited()) {
            creeper.ignite();
        }
    }

    @EventHandler
    public void onCreeperExplode(EntityExplodeEvent event) {
        if (!configManager.isCreeperGriefingDisabled()) return;

        if (!(event.getEntity() instanceof Creeper)) return;
        event.blockList().clear();
    }
}