package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.ConfigManager;
import dev.jaegyu.homeBase.enchantments.HarvestingEnchant;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

        if (!isHoe(tool.getType())) return;

        int level = tool.getEnchantmentLevel(HarvestingEnchant.get());
        if (level <= 0) return;

        Block block = event.getBlock();
        if (!HOE_BLOCKS.contains(block.getType())) return;

        double bonusChance = HarvestingEnchant.BONUS_PER_LEVEL * level;

        Collection<ItemStack> drops = block.getDrops(tool, player);
        for (ItemStack drop : drops) {
            if (RANDOM.nextDouble() < bonusChance) {
                block.getWorld().dropItemNaturally(block.getLocation(), drop.clone());
            }
        }
    }

    private boolean isHoe(Material mat) {
        return mat == Material.WOODEN_HOE || mat == Material.STONE_HOE
                || mat == Material.IRON_HOE || mat == Material.GOLDEN_HOE
                || mat == Material.DIAMOND_HOE || mat == Material.NETHERITE_HOE;
    }
}