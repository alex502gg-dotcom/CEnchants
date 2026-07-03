package ru.customenchants.listener;

import org.bukkit.Material;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import ru.customenchants.CustomEnchantmentsPlugin;
import ru.customenchants.manager.EnchantmentManager;

public final class AnvilApplyListener implements Listener {
    private final CustomEnchantmentsPlugin plugin;
    private final EnchantmentManager enchantments;

    public AnvilApplyListener(CustomEnchantmentsPlugin plugin, EnchantmentManager enchantments) {
        this.plugin = plugin;
        this.enchantments = enchantments;
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack target = event.getInventory().getFirstItem();
        ItemStack book = event.getInventory().getSecondItem();
        if (target == null || target.getType() == Material.AIR) {
            return;
        }
        enchantments.readBook(book).ifPresent(bookData -> {
            ItemStack result = target.clone();
            if (!enchantments.apply(result, bookData.enchantment(), 1)) {
                return;
            }
            event.setResult(result);
            event.getInventory().setRepairCost(1);
        });
    }

    @EventHandler
    public void onTakeAnvilResult(InventoryClickEvent event) {
        if (event.getView().getTopInventory().getType() != InventoryType.ANVIL || event.getRawSlot() != 2) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack target = event.getView().getTopInventory().getItem(0);
        ItemStack book = event.getView().getTopInventory().getItem(1);
        enchantments.readBook(book).ifPresent(bookData -> {
            if (target == null || target.getType() == Material.AIR) {
                return;
            }
            if (player.getGameMode() != GameMode.CREATIVE && player.getLevel() < 1) {
                return;
            }

            ItemStack result = target.clone();
            if (!enchantments.apply(result, bookData.enchantment(), 1)) {
                return;
            }

            event.setCancelled(true);
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.setLevel(player.getLevel() - 1);
            }
            event.getView().getTopInventory().setItem(0, null);
            int newBookAmount = book.getAmount() - 1;
            if (newBookAmount > 0) {
                book.setAmount(newBookAmount);
                event.getView().getTopInventory().setItem(1, book);
            } else {
                event.getView().getTopInventory().setItem(1, null);
            }

            if (event.isShiftClick()) {
                player.getInventory().addItem(result).values()
                        .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            } else if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
                event.setCursor(result);
            } else {
                player.getInventory().addItem(result).values()
                        .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            }
            player.updateInventory();
        });
    }
}
