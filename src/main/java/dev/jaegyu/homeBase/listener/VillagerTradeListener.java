
package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.ConfigManager;
import dev.jaegyu.homeBase.enchantments.HarvestingEnchant;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Random;

public class VillagerTradeListener implements Listener {

    private static final Random RANDOM = new Random();
    private final ConfigManager configManager;
    private final Plugin plugin;

    public VillagerTradeListener(Plugin plugin, ConfigManager configManager) {
        this.configManager = configManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if (!configManager.isHarvestingEnabled()) return;

        if (!(event.getEntity() instanceof Villager villager)) return;
        if (villager.getProfession() != Villager.Profession.LIBRARIAN) return;

        // ~15% chance this trade slot becomes a Harvesting book
        if (RANDOM.nextDouble() > 0.15) return;

        // Level 1–4, weighted toward lower levels
        int level = RANDOM.nextInt(4) + 1;

        // Build the enchanted book
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        HarvestingEnchant.setLevel(book, level);

        // Price scales with level: 10/15/20/25 emeralds
        int emeraldCost = 5 + (level * 5);
        ItemStack price = new ItemStack(Material.EMERALD, emeraldCost);

        MerchantRecipe recipe = new MerchantRecipe(book, 0, 4, true);
        recipe.addIngredient(new ItemStack(Material.BOOK));
        recipe.addIngredient(price);

        event.setRecipe(recipe);
    }
}