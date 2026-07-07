package ru.customenchants.listener;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import ru.customenchants.CustomEnchantmentsPlugin;
import ru.customenchants.builtin.AutoSmeltEnchantment;
import ru.customenchants.builtin.MagnetismEnchantment;
import ru.customenchants.manager.EnchantmentManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class BlockDropListener implements Listener {
    private final CustomEnchantmentsPlugin plugin;
    private final EnchantmentManager enchantments;

    public BlockDropListener(CustomEnchantmentsPlugin plugin, EnchantmentManager enchantments) {
        this.plugin = plugin;
        this.enchantments = enchantments;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        int autoSmeltLevel = enchantments.level(tool, "autosmelt");
        int magnetLevel = enchantments.level(tool, "magnetism");
        if (autoSmeltLevel <= 0 && magnetLevel <= 0) {
            return;
        }

        AutoSmeltEnchantment autoSmelt = enchantments.find("autosmelt")
                .filter(AutoSmeltEnchantment.class::isInstance)
                .map(AutoSmeltEnchantment.class::cast)
                .orElse(null);
        MagnetismEnchantment magnetism = enchantments.find("magnetism")
                .filter(MagnetismEnchantment.class::isInstance)
                .map(MagnetismEnchantment.class::cast)
                .orElse(null);

        boolean shouldSmelt = autoSmeltLevel > 0 && autoSmelt != null && !autoSmelt.shouldSkip(tool);
        boolean shouldCollect = magnetLevel > 0 && magnetism != null && magnetism.collectBlockDrops();
        if (!shouldSmelt && !shouldCollect) {
            return;
        }

        List<ItemStack> drops = new ArrayList<>(event.getBlock().getDrops(tool));
        if (drops.isEmpty()) {
            event.setDropItems(false);
            return;
        }

        int exp = Math.max(0, event.getExpToDrop());
        if (shouldSmelt) {
            List<ItemStack> smelted = new ArrayList<>();
            for (ItemStack drop : drops) {
                exp += autoSmelt.expFor(drop);
                smelted.add(autoSmelt.smelt(drop));
            }
            drops = smelted;
        }

        event.setDropItems(false);
        event.setExpToDrop(0);
        if (exp > 0) {
            player.giveExp(exp);
        }

        if (shouldCollect) {
            giveOrDrop(player, drops, event.getBlock().getLocation());
            return;
        }
        for (ItemStack drop : drops) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        int magnetLevel = enchantments.level(tool, "magnetism");
        if (magnetLevel <= 0) {
            return;
        }
        enchantments.find("magnetism")
                .filter(MagnetismEnchantment.class::isInstance)
                .map(MagnetismEnchantment.class::cast)
                .filter(MagnetismEnchantment::collectBlockDrops)
                .ifPresent(magnetism -> collectDrops(event.getItems(), player, event.getBlock().getLocation()));
    }

    private void collectDrops(List<Item> drops, Player player, Location fallbackLocation) {
        Iterator<Item> iterator = drops.iterator();
        while (iterator.hasNext()) {
            Item drop = iterator.next();
            giveOrDrop(player, List.of(drop.getItemStack()), fallbackLocation);
            iterator.remove();
            drop.remove();
        }
    }

    private void giveOrDrop(Player player, List<ItemStack> items, Location fallbackLocation) {
        for (ItemStack itemStack : items) {
            Map<Integer, ItemStack> leftoverMap = player.getInventory().addItem(itemStack);
            for (ItemStack leftover : leftoverMap.values()) {
                Item dropped = player.getWorld().dropItemNaturally(fallbackLocation, leftover);
                dropped.setPickupDelay(40);
            }
        }
    }
}
