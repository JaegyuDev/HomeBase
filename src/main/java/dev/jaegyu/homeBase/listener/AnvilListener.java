package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.meta.Repairable;

public class AnvilListener implements Listener {

    private final ConfigManager configManager;

    public AnvilListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Clamps the stored repair penalty on any item coming out of the anvil.
     * Prevents "Too Expensive!" from blocking legitimate repairs.
     */
    @EventHandler
    public void onAnvilRepairCost(PrepareAnvilEvent event) {
        if(!configManager.isAnvilXPCapped()) return;

        var result = event.getResult();
        if (result == null) return;

        var meta = result.getItemMeta();
        if (!(meta instanceof Repairable repairable)) return;

        if (repairable.getRepairCost() > 40) {
            repairable.setRepairCost(32);
            result.setItemMeta(meta);
            event.setResult(result);
            event.getView().setRepairCost(32);
        }
    }
}