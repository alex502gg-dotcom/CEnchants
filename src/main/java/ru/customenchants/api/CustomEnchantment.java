package ru.customenchants.api;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public interface CustomEnchantment {
    String key();

    String displayName();

    int maxLevel();

    default boolean canApply(ItemStack item) {
        return item != null && !item.getType().isAir();
    }

    default void onRegister(Plugin plugin, ConfigurationSection config) {
    }

    default void onUnregister() {
    }
}
