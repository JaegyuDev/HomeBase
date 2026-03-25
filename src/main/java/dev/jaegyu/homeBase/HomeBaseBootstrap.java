package dev.jaegyu.homeBase;

import dev.jaegyu.homeBase.enchantments.HarvestingEnchant;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.registry.data.EnchantmentRegistryEntry;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class HomeBaseBootstrap implements PluginBootstrap {

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(
                RegistryEvents.ENCHANTMENT.compose().newHandler(event -> {
                    event.registry().register(
                            HarvestingEnchant.KEY,
                            b -> b.description(Component.text("Harvesting"))
                                    .supportedItems(event.getOrCreateTag(ItemTypeTagKeys.HOES))
                                    .anvilCost(2)
                                    .maxLevel(HarvestingEnchant.MAX_LEVEL)
                                    .weight(5)
                                    .minimumCost(EnchantmentRegistryEntry.EnchantmentCost.of(15, 9))
                                    .maximumCost(EnchantmentRegistryEntry.EnchantmentCost.of(65, 9))
                                    .activeSlots(EquipmentSlotGroup.MAINHAND)
                    );
                })
        );
    }
}