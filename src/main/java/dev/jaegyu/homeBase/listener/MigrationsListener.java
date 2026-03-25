package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.enchantments.HarvestingEnchant;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class MigrationsListener implements Listener {

    private static final NamespacedKey OLD_KEY = new NamespacedKey("homebase", "harvesting");

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (ItemStack item : player.getInventory().getContents()) {
            migrate(item);
        }
    }

    private void migrate(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;

        var meta = item.getItemMeta();
        var pdc = meta.getPersistentDataContainer();

        // Check for the old PDC key
        if (!pdc.has(OLD_KEY, PersistentDataType.INTEGER)) return;

        int level = pdc.getOrDefault(OLD_KEY, PersistentDataType.INTEGER, 0);
        if (level <= 0) return;

        // Remove old PDC data and lore line
        pdc.remove(OLD_KEY);
        if (meta.hasLore()) {
            meta.setLore(meta.getLore().stream()
                    .filter(line -> !line.contains("Harvesting"))
                    .toList());
        }
        item.setItemMeta(meta);

        // Apply new registry enchantment
        item.addUnsafeEnchantment(HarvestingEnchant.get(), level);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        migrate(event.getCurrentItem());
    }

    @EventHandler
    public void onEntityPickup(EntityPickupItemEvent event) {
        migrate(event.getItem().getItemStack());
    }
}
