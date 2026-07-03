package ru.customenchants.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ru.customenchants.CustomEnchantmentsPlugin;
import ru.customenchants.manager.EnchantmentManager;

public final class BookApplyListener implements Listener {
    private final CustomEnchantmentsPlugin plugin;
    private final EnchantmentManager enchantments;

    public BookApplyListener(CustomEnchantmentsPlugin plugin, EnchantmentManager enchantments) {
        this.plugin = plugin;
        this.enchantments = enchantments;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ItemStack cursor = event.getCursor();
        ItemStack target = event.getCurrentItem();
        enchantments.readBook(cursor).ifPresent(book -> {
            if (target == null || target.getType() == Material.AIR) {
                return;
            }
            if (!book.enchantment().canApply(target)) {
                player.sendMessage(enchantments.prefixed("cannot-apply"));
                return;
            }
            event.setCancelled(true);
            if (!enchantments.apply(target, book.enchantment(), book.level())) {
                player.sendMessage(enchantments.prefixed("cannot-apply"));
                return;
            }
            cursor.setAmount(cursor.getAmount() - 1);
            event.setCursor(cursor.getAmount() > 0 ? cursor : null);
            event.setCurrentItem(target);
            player.updateInventory();
            player.sendMessage(enchantments.prefixed("applied")
                    .replace("%display_name%", book.enchantment().displayName()));
        });
    }
}
