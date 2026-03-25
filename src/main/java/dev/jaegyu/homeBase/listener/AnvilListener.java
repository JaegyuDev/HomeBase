package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.ConfigManager;
import dev.jaegyu.homeBase.enchantments.HarvestingEnchant;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

public class AnvilListener implements Listener {
    private final ConfigManager configManager;

    public AnvilListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!configManager.isHarvestingEnabled()) return;

        AnvilInventory inv = event.getInventory();
        ItemStack left = inv.getItem(0);   // tool
        ItemStack right = inv.getItem(1);  // book

        if (left == null || right == null) return;
        if (!HarvestingEnchant.isHoe(left.getType())) return;
        if (right.getType() != Material.ENCHANTED_BOOK) return;

        int bookLevel = HarvestingEnchant.getLevel(right);
        if (bookLevel == 0) return;

        int toolLevel = HarvestingEnchant.getLevel(left);

        // Same level + same level = upgrade (up to max), otherwise take the higher
        int resultLevel;
        if (toolLevel == bookLevel && toolLevel < HarvestingEnchant.MAX_LEVEL) {
            resultLevel = toolLevel + 1;
        } else {
            resultLevel = Math.min(Math.max(toolLevel, bookLevel), HarvestingEnchant.MAX_LEVEL);
        }

        // If nothing would change, don't interfere
        if (resultLevel == toolLevel) return;

        ItemStack result = left.clone();
        HarvestingEnchant.setLevel(result, resultLevel);

        event.setResult(result);

        // Set a repair cost so the anvil charges levels
        inv.setRepairCost(bookLevel * 3);
    }
}