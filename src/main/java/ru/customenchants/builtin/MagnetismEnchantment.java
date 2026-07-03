package ru.customenchants.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.customenchants.api.CustomEnchantment;

public final class MagnetismEnchantment implements CustomEnchantment {
    private String displayName = "Магнетизм";
    private boolean collectBlockDrops = true;
    private double pickupRadius = 6.0D;

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
        pickupRadius = Math.max(0.0D, config.getDouble("pickup-radius", pickupRadius));
    }

    public boolean collectBlockDrops() {
        return collectBlockDrops;
    }

    public double radius() {
        return pickupRadius;
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && !item.getType().isAir();
    }
}
