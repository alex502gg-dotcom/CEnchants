package ru.customenchants.builtin;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.customenchants.api.CustomEnchantment;

public final class MagnetismEnchantment implements CustomEnchantment {
    private final Plugin plugin;
    private String displayName = "Магнетизм";
    private int maxLevel = 3;
    private boolean collectBlockDrops = true;
    private double pickupRadiusPerLevel = 2.5D;

    public MagnetismEnchantment(Plugin plugin) {
        this.plugin = plugin;
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
    public int maxLevel() {
        return maxLevel;
    }

    @Override
    public void onRegister(Plugin plugin, ConfigurationSection config) {
        if (config == null) {
            return;
        }
        displayName = config.getString("display-name", displayName);
        maxLevel = Math.max(1, config.getInt("max-level", maxLevel));
        collectBlockDrops = config.getBoolean("collect-block-drops", collectBlockDrops);
        pickupRadiusPerLevel = Math.max(0.0D, config.getDouble("pickup-radius-per-level", pickupRadiusPerLevel));
    }

    public boolean collectBlockDrops() {
        return collectBlockDrops;
    }

    public double radius(int level) {
        return pickupRadiusPerLevel * Math.max(1, level);
    }

    @Override
    public boolean canApply(ItemStack item) {
        return item != null && !item.getType().isAir();
    }
}
