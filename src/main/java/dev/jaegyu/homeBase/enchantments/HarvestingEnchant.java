package dev.jaegyu.homeBase.enchantments;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.enchantments.Enchantment;

public final class HarvestingEnchant {

    public static final int MAX_LEVEL = 4;
    public static final double BONUS_PER_LEVEL = 0.125; // 12.5%

    public static final io.papermc.paper.registry.TypedKey<Enchantment> KEY =
            io.papermc.paper.registry.TypedKey.create(
                    RegistryKey.ENCHANTMENT,
                    Key.key("homebase:harvesting")
            );

    private HarvestingEnchant() {}

    /** Returns the live Enchantment object from the registry. Call after bootstrap. */
    public static Enchantment get() {
        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .getOrThrow(KEY);
    }

    /** Calculates the drop multiplier for a given level. e.g. level 2 → 1.25 */
    public static double getMultiplier(int level) {
        return 1.0 + (BONUS_PER_LEVEL * level);
    }
}