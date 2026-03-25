package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.ConfigManager;
import dev.jaegyu.homeBase.enchantments.HarvestingEnchant;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Random;

public class HarvestingListener implements Listener {

    private static final Random RANDOM = new Random();
    private final ConfigManager configManager;

    // All blocks that are "harvested" with a hoe
    private static final java.util.Set<Material> HOE_BLOCKS = java.util.Set.of(
            Material.WHEAT, Material.CARROTS, Material.POTATOES, Material.BEETROOTS,
            Material.NETHER_WART, Material.COCOA, Material.SWEET_BERRY_BUSH,
            Material.PUMPKIN, Material.MELON, Material.SUGAR_CANE,
            Material.SHORT_GRASS, Material.TALL_GRASS, Material.FERN,
            Material.LARGE_FERN, Material.DEAD_BUSH, Material.MOSS_BLOCK,
            Material.SCULK, Material.WARPED_WART_BLOCK, Material.NETHER_WART_BLOCK,
            Material.HAY_BLOCK, Material.DRIED_KELP_BLOCK, Material.SPONGE, Material.WET_SPONGE
    );

    public HarvestingListener(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!configManager.isHarvestingEnabled()) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!HarvestingEnchant.isHoe(tool.getType())) return;

        int level = HarvestingEnchant.getLevel(tool);
        if (level <= 0) return;

        Block block = event.getBlock();
        if (!HOE_BLOCKS.contains(block.getType())) return;

        double bonusChance = HarvestingEnchant.BONUS_PER_LEVEL * level; // e.g. 0.25 at level 2

        // Get what the block would naturally drop
        Collection<ItemStack> drops = block.getDrops(tool, player);

        // For each drop, give a chance at a bonus copy
        for (ItemStack drop : drops) {
            if (RANDOM.nextDouble() < bonusChance) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop.clone());
            }
        }
    }
}