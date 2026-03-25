package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.ConfigManager;
import dev.jaegyu.homeBase.enchantments.HarvestingEnchant;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class EnchantTableListener implements Listener {

    private static final Random RANDOM = new Random();
    private final ConfigManager configManager;

    public EnchantTableListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        if (!configManager.isHarvestingEnabled()) return;

        ItemStack item = event.getItem();
        if (!isHoe(item.getType())) return;

        // ~30% chance to get Harvesting when enchanting a hoe
        if (RANDOM.nextDouble() > 0.30) return;

        // Level scales loosely with enchantment cost (button 1/2/3 → levels 1–4)
        int expCost = event.getExpLevelCost();
        int level;
        if (expCost >= 30) level = 4;
        else if (expCost >= 20) level = 3;
        else if (expCost >= 10) level = 2;
        else level = 1;

        // addEnchantsToItem runs after the event, so we add directly to the item here.
        // addUnsafeEnchantment bypasses the canEnchant check, which is fine since we
        // already guard with isHoe above.
        event.getEnchantsToAdd().put(HarvestingEnchant.get(), level);
    }

    private boolean isHoe(org.bukkit.Material mat) {
        return mat == org.bukkit.Material.WOODEN_HOE || mat == org.bukkit.Material.STONE_HOE
                || mat == org.bukkit.Material.IRON_HOE || mat == org.bukkit.Material.GOLDEN_HOE
                || mat == org.bukkit.Material.DIAMOND_HOE || mat == org.bukkit.Material.NETHERITE_HOE;
    }
}