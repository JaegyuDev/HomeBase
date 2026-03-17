package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class EntityDeathListener implements Listener {

    /*
    *  This is added because I thought it'd be cool if ghasts dropped additional loot if they
    *  were killed by their own fireballs.
    *
    *    - Jae, 02/24/26
    * */

    private final ConfigManager configManager;

    public EntityDeathListener(ConfigManager configManager) {
        this.configManager = configManager;
    }


    @EventHandler
    public void onGhastDeath(EntityDeathEvent e) {
        if (!configManager.isReturnToSenderLootEnabled()) return;

        if (!(e.getEntity() instanceof Ghast ghast)) return;

        EntityDamageEvent lastDamage = ghast.getLastDamageCause();
        if (!(lastDamage instanceof EntityDamageByEntityEvent damageByEntity)) return;

        if (!(damageByEntity.getDamager() instanceof Fireball fireball)) return;
        if (!(fireball.getShooter() instanceof Ghast)) return;

        e.getDrops().clear();

        Random random = ThreadLocalRandom.current();

        int gunpowder = random.nextInt(3) + random.nextInt(4);
        int tears = random.nextInt(2) + random.nextInt(4);
        if (gunpowder > 0) {
            e.getDrops().add(new ItemStack(Material.GUNPOWDER, gunpowder));
        }

        if (tears > 0) {
            e.getDrops().add(new ItemStack(Material.GHAST_TEAR, tears));
        }
    }
}
