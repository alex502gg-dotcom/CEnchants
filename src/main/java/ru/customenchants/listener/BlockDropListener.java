package ru.customenchants.listener;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import ru.customenchants.CustomEnchantmentsPlugin;
import ru.customenchants.builtin.AutoSmeltEnchantment;
import ru.customenchants.builtin.MagnetismEnchantment;
import ru.customenchants.manager.EnchantmentManager;

import java.util.ArrayList;
import java.util.Collection;
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
    public void onBlockDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        int autoSmeltLevel = enchantments.level(tool, "autosmelt");
        int magnetLevel = enchantments.level(tool, "magnetism");

        if (autoSmeltLevel > 0) {
            enchantments.find("autosmelt")
                    .filter(AutoSmeltEnchantment.class::isInstance)
                    .map(AutoSmeltEnchantment.class::cast)
                    .filter(autoSmelt -> !autoSmelt.shouldSkip(tool))
                    .ifPresent(autoSmelt -> event.getItems().forEach(item -> item.setItemStack(autoSmelt.smelt(item.getItemStack()))));
        }

        if (magnetLevel <= 0) {
            return;
        }
        enchantments.find("magnetism")
                .filter(MagnetismEnchantment.class::isInstance)
                .map(MagnetismEnchantment.class::cast)
                .filter(MagnetismEnchantment::collectBlockDrops)
                .ifPresent(magnetism -> collectDrops(event.getItems(), player, event.getBlock().getLocation()));
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onAttemptPickup(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        int level = enchantments.level(tool, "magnetism");
        if (level <= 0) {
            return;
        }
        enchantments.find("magnetism")
                .filter(MagnetismEnchantment.class::isInstance)
                .map(MagnetismEnchantment.class::cast)
                .ifPresent(magnetism -> collectNearby(player, magnetism.radius(level)));
    }

    private void collectDrops(List<Item> drops, Player player, Location fallbackLocation) {
        Iterator<Item> iterator = drops.iterator();
        while (iterator.hasNext()) {
            Item drop = iterator.next();
            List<ItemStack> leftovers = addToInventory(player, drop.getItemStack());
            iterator.remove();
            drop.remove();
            for (ItemStack leftover : leftovers) {
                player.getWorld().dropItemNaturally(fallbackLocation, leftover);
            }
        }
    }

    private void collectNearby(Player player, double radius) {
        Collection<Item> nearby = player.getWorld().getNearbyEntitiesByType(Item.class, player.getLocation(), radius);
        for (Item item : nearby) {
            if (item.isDead() || !item.isValid()) {
                continue;
            }
            List<ItemStack> leftovers = addToInventory(player, item.getItemStack());
            item.remove();
            for (ItemStack leftover : leftovers) {
                player.getWorld().dropItemNaturally(item.getLocation(), leftover);
            }
        }
    }

    private List<ItemStack> addToInventory(Player player, ItemStack itemStack) {
        Map<Integer, ItemStack> leftoverMap = player.getInventory().addItem(itemStack);
        return new ArrayList<>(leftoverMap.values());
    }
}
