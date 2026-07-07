package ru.customenchants.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.customenchants.api.CustomEnchantment;

public final class MagnetismEnchantment implements CustomEnchantment {
    private String displayName = "Магнетизм";
    private boolean collectBlockDrops = true;

    public MagnetismEnchantment(Plugin plugin) {
    }

    @Override
    public String key() {
        return "magnetism";
    }

    @Override
    public String displayName() {
        return displayName;
    }

    @Override
    public void onRegister(Plugin plugin, ConfigurationSection config) {
        if (config == null) {
            return;
        }
        displayName = config.getString("display-name", displayName);
        collectBlockDrops = config.getBoolean("collect-block-drops", collectBlockDrops);
    }

    public boolean collectBlockDrops() {
        return collectBlockDrops;
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && !item.getType().isAir();
    }
}
