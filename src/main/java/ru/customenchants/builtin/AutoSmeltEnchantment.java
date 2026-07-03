package ru.customenchants.builtin;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.customenchants.api.CustomEnchantment;

import java.util.EnumMap;
import java.util.Map;

public final class AutoSmeltEnchantment implements CustomEnchantment {
    private final Map<Material, Material> smelts = new EnumMap<>(Material.class);
    private String displayName = "Автоплавка";
    private boolean respectSilkTouch = true;
    private boolean giveExp = true;
    private int expPerSmeltedItem = 1;

    public AutoSmeltEnchantment(Plugin plugin) {
        smelts.put(Material.RAW_IRON, Material.IRON_INGOT);
        smelts.put(Material.RAW_GOLD, Material.GOLD_INGOT);
        smelts.put(Material.RAW_COPPER, Material.COPPER_INGOT);
        smelts.put(Material.COBBLESTONE, Material.STONE);
        smelts.put(Material.COBBLED_DEEPSLATE, Material.DEEPSLATE);
        smelts.put(Material.SAND, Material.GLASS);
        smelts.put(Material.RED_SAND, Material.GLASS);
        smelts.put(Material.CLAY_BALL, Material.BRICK);
        smelts.put(Material.CACTUS, Material.GREEN_DYE);
        smelts.put(Material.WET_SPONGE, Material.SPONGE);
        smelts.put(Material.PORKCHOP, Material.COOKED_PORKCHOP);
        smelts.put(Material.BEEF, Material.COOKED_BEEF);
        smelts.put(Material.CHICKEN, Material.COOKED_CHICKEN);
        smelts.put(Material.MUTTON, Material.COOKED_MUTTON);
        smelts.put(Material.RABBIT, Material.COOKED_RABBIT);
        smelts.put(Material.COD, Material.COOKED_COD);
        smelts.put(Material.SALMON, Material.COOKED_SALMON);
        smelts.put(Material.POTATO, Material.BAKED_POTATO);
        smelts.put(Material.KELP, Material.DRIED_KELP);
        smelts.put(Material.NETHERRACK, Material.NETHER_BRICK);
    }

    @Override
    public String key() {
        return "autosmelt";
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
        respectSilkTouch = config.getBoolean("respect-silk-touch", respectSilkTouch);
        giveExp = config.getBoolean("give-exp", giveExp);
        expPerSmeltedItem = Math.max(0, config.getInt("exp-per-smelted-item", expPerSmeltedItem));
    }

    public boolean shouldSkip(ItemStack tool) {
        return respectSilkTouch && tool != null && tool.containsEnchantment(Enchantment.SILK_TOUCH);
    }

    public ItemStack smelt(ItemStack item) {
        Material result = smelts.get(item.getType());
        if (result == null) {
            return item;
        }
        return new ItemStack(result, item.getAmount());
    }

    public int expFor(ItemStack original) {
        if (!giveExp || !smelts.containsKey(original.getType())) {
            return 0;
        }
        return original.getAmount() * expPerSmeltedItem;
    }
}
