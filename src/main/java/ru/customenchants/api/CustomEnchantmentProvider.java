package ru.customenchants.api;

import org.bukkit.plugin.Plugin;

import java.util.Collection;

public interface CustomEnchantmentProvider {
    Collection<CustomEnchantment> createEnchantments(Plugin plugin);
}
