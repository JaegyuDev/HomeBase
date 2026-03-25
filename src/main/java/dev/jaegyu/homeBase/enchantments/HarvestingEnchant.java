package dev.jaegyu.homeBase.enchantments;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class HarvestingEnchant {

    public static final int MAX_LEVEL = 4;
    public static final double BONUS_PER_LEVEL = 0.125; // 12.5%

    private static NamespacedKey key;

    public static void init(JavaPlugin plugin) {
        key = new NamespacedKey(plugin, "harvesting");
    }

    public static NamespacedKey getKey() {
        return key;
    }

    /** Returns the Harvesting level on the item, or 0 if none. */
    public static int getLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.getOrDefault(key, PersistentDataType.INTEGER, 0);
    }

    /** Sets the Harvesting level on the item (0 removes it). */
    public static void setLevel(ItemStack item, int level) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (level <= 0) {
            pdc.remove(key);
        } else {
            pdc.set(key, PersistentDataType.INTEGER, Math.min(level, MAX_LEVEL));
        }

        // Rebuild lore — remove old Harvesting line, add new one
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> line.contains("Harvesting"));
        if (level > 0) {
            lore.add("§7Harvesting " + toRoman(level));
        }
        meta.setLore(lore);

        // If it's an enchanted book, add a hidden dummy enchant for the glow
        if (item.getType() == Material.ENCHANTED_BOOK) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
    }

    /** Calculates the drop multiplier for a given level. e.g. level 2 → 1.25 */
    public static double getMultiplier(int level) {
        return 1.0 + (BONUS_PER_LEVEL * level);
    }

    public static boolean isHoe(Material mat) {
        return mat == Material.WOODEN_HOE || mat == Material.STONE_HOE
                || mat == Material.IRON_HOE || mat == Material.GOLDEN_HOE
                || mat == Material.DIAMOND_HOE || mat == Material.NETHERITE_HOE;
    }

    private static String toRoman(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            default -> String.valueOf(n);
        };
    }
}