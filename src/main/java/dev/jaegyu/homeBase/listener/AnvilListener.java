package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.ConfigManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnvilListener implements Listener {

    private final ConfigManager configManager;

    // Tracks how much leather was consumed for the last elytra repair preview,
    // keyed by player UUID. Cleared when the result is taken or the inventory closes.
    private final Map<UUID, Integer> pendingLeatherCost = new HashMap<>();

    public AnvilListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Clamps the stored repair penalty on any item coming out of the anvil.
     * Prevents "Too Expensive!" from blocking legitimate repairs.
     */
    @EventHandler
    public void onAnvilRepairCost(PrepareAnvilEvent event) {
        var result = event.getResult();
        if (result == null) return;

        var meta = result.getItemMeta();
        if (!(meta instanceof Repairable repairable)) return;

        if (repairable.getRepairCost() > 40) {
            repairable.setRepairCost(40);
            result.setItemMeta(meta);
            event.setResult(result);
            event.getView().setRepairCost(40);
        }
    }
}