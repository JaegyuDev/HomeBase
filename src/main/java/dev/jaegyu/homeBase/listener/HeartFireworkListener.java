package dev.jaegyu.homeBase.listener;

import dev.jaegyu.homeBase.ConfigManager;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumMap;
import java.util.Map;

public class HeartFireworkListener implements Listener {

    // PDC key stored on the firework star item and copied onto the rocket item at craft time.
    // At explosion time we read it off the rocket entity's item via Firework#getItem().
    public static final NamespacedKey HEART_COLOR_KEY =
            new NamespacedKey("homebase", "heart_firework_color");

    // Maps each dye material to the Bukkit Color it represents.
    private static final Map<Material, Color> DYE_COLORS = new EnumMap<>(Material.class);

    static {
        DYE_COLORS.put(Material.WHITE_DYE,      Color.WHITE);
        DYE_COLORS.put(Material.ORANGE_DYE,     Color.ORANGE);
        DYE_COLORS.put(Material.MAGENTA_DYE,    Color.fromRGB(0xFF55FF));
        DYE_COLORS.put(Material.LIGHT_BLUE_DYE, Color.fromRGB(0x55FFFF));
        DYE_COLORS.put(Material.YELLOW_DYE,     Color.YELLOW);
        DYE_COLORS.put(Material.LIME_DYE,       Color.LIME);
        DYE_COLORS.put(Material.PINK_DYE,       Color.fromRGB(0xFF69B4));
        DYE_COLORS.put(Material.GRAY_DYE,       Color.GRAY);
        DYE_COLORS.put(Material.LIGHT_GRAY_DYE, Color.SILVER);
        DYE_COLORS.put(Material.CYAN_DYE,       Color.TEAL);
        DYE_COLORS.put(Material.PURPLE_DYE,     Color.PURPLE);
        DYE_COLORS.put(Material.BLUE_DYE,       Color.BLUE);
        DYE_COLORS.put(Material.BROWN_DYE,      Color.fromRGB(0x8B4513));
        DYE_COLORS.put(Material.GREEN_DYE,      Color.GREEN);
        DYE_COLORS.put(Material.RED_DYE,        Color.RED);
        DYE_COLORS.put(Material.BLACK_DYE,      Color.BLACK);
    }

    private final ConfigManager configManager;
    private final JavaPlugin plugin;

    public HeartFireworkListener(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        registerRecipes();
    }

    // -------------------------------------------------------------------------
    // Recipe registration — one recipe per dye color so all 16 show in the
    // crafting book, matching how vanilla firework stars work.
    // -------------------------------------------------------------------------

    private void registerRecipes() {
        for (Map.Entry<Material, Color> entry : DYE_COLORS.entrySet()) {
            Material dye = entry.getKey();
            Color color = entry.getValue();

            // Result: a FIREWORK_STAR pre-tagged with our PDC color
            ItemStack result = new ItemStack(Material.FIREWORK_STAR);
            ItemMeta meta = result.getItemMeta();
            meta.getPersistentDataContainer().set(
                    HEART_COLOR_KEY, PersistentDataType.INTEGER, color.asRGB());
            result.setItemMeta(meta);

            NamespacedKey key = new NamespacedKey(
                    "homebase", "heart_firework_star_" + dye.name().toLowerCase());

            ShapelessRecipe recipe = new ShapelessRecipe(key, result);
            recipe.addIngredient(Material.GUNPOWDER);
            recipe.addIngredient(Material.PINK_PETALS);
            recipe.addIngredient(dye);

            Bukkit.addRecipe(recipe);
        }
    }

    // -------------------------------------------------------------------------
    // Crafting — when a player combines a heart star into a FIREWORK_ROCKET,
    // we copy the PDC tag onto the rocket item. By the time FireworkExplodeEvent
    // fires, the only item we can inspect is the rocket via Firework#getItem(),
    // so this transfer step is necessary.
    // -------------------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!configManager.isHeartFireworkEnabled()) return;

        ItemStack result = event.getCurrentItem();
        if (result == null || result.getType() != Material.FIREWORK_ROCKET) return;

        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient == null || ingredient.getType() != Material.FIREWORK_STAR) continue;
            if (!ingredient.hasItemMeta()) continue;

            var pdc = ingredient.getItemMeta().getPersistentDataContainer();
            if (!pdc.has(HEART_COLOR_KEY, PersistentDataType.INTEGER)) continue;

            int rgb = pdc.get(HEART_COLOR_KEY, PersistentDataType.INTEGER);

            ItemMeta rocketMeta = result.getItemMeta();
            rocketMeta.getPersistentDataContainer()
                    .set(HEART_COLOR_KEY, PersistentDataType.INTEGER, rgb);
            result.setItemMeta(rocketMeta);
            event.setCurrentItem(result);
            break; // one heart star per rocket is enough
        }
    }

    // -------------------------------------------------------------------------
    // Explosion — cancel vanilla particles, bloom a heart in DUST particles.
    // -------------------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onFireworkExplode(FireworkExplodeEvent event) {
        if (!configManager.isHeartFireworkEnabled()) return;

        Firework firework = event.getEntity();
        ItemStack rocketItem = firework.getItem();
        if (rocketItem == null || !rocketItem.hasItemMeta()) return;

        var pdc = rocketItem.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(HEART_COLOR_KEY, PersistentDataType.INTEGER)) return;

        int rgb = pdc.get(HEART_COLOR_KEY, PersistentDataType.INTEGER);
        Color color = Color.fromRGB(rgb);

        // Suppress vanilla explosion so our particles are the only display.
        // Per the Paper docs, cancelling FireworkExplodeEvent still removes the
        // entity — it only skips the vanilla particle burst.
        event.setCancelled(true);

        Location origin = firework.getLocation();
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.8f);

        // Bloom over 3 ticks: scale grows 2.4 → 4.0 → 5.6 to mimic the
        // expanding ring of a real firework burst.
        new BukkitRunnable() {
            int tick = 0;
            final double[] scales = {2.4, 4.0, 5.6};

            @Override
            public void run() {
                if (tick >= scales.length) {
                    cancel();
                    return;
                }
                spawnHeart(origin, dust, scales[tick]);
                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Play vanilla firework sounds so it still feels like a firework
        origin.getWorld().playSound(
                origin, Sound.ENTITY_FIREWORK_ROCKET_BLAST,
                SoundCategory.AMBIENT, 3.0f, 1.0f);
        origin.getWorld().playSound(
                origin, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE,
                SoundCategory.AMBIENT, 2.0f, 1.0f);
    }

    // -------------------------------------------------------------------------
    // Heart math — parametric heart curve oriented in the XZ plane (horizontal)
    // so it reads as a bloom in the sky, not a vertical wall.
    //
    // Formula: x = 16sin³(t), y = 13cos(t) - 5cos(2t) - 2cos(3t) - cos(4t)
    // Raw extents are ~[-17, 17] on X and ~[-17, 13] on Y; we normalise to
    // ~1 block radius so `scale` is intuitive (scale=1.0 ≈ 1 block radius).
    // -------------------------------------------------------------------------

    private void spawnHeart(Location origin, Particle.DustOptions dust, double scale) {
        World world = origin.getWorld();
        double normalizer = 17.0;

        // 0.07 rad step ≈ 90 points — keeps density consistent at the larger size
        for (double t = 0; t < 2 * Math.PI; t += 0.07) {
            double rawX = 16.0 * Math.pow(Math.sin(t), 3);
            double rawY = 13.0 * Math.cos(t)
                    - 5.0 * Math.cos(2 * t)
                    - 2.0 * Math.cos(3 * t)
                    - Math.cos(4 * t);

            double x = (rawX / normalizer) * scale;
            double y = (rawY / normalizer) * scale;

            // Map to XZ (horizontal) so the heart is flat in the sky
            Location point = origin.clone().add(x, y, 0);
            world.spawnParticle(Particle.DUST, point, 1, 0, 0, 0, 0, dust);
        }
    }
}