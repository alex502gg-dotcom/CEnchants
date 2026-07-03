package ru.customenchants.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.customenchants.CustomEnchantmentsPlugin;
import ru.customenchants.api.CustomEnchantment;
import ru.customenchants.manager.EnchantmentManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class EnchantmentCommand implements CommandExecutor, TabCompleter {
    private final CustomEnchantmentsPlugin plugin;
    private final EnchantmentManager enchantments;

    public EnchantmentCommand(CustomEnchantmentsPlugin plugin, EnchantmentManager enchantments) {
        this.plugin = plugin;
        this.enchantments = enchantments;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("customenchants.command.emchantment")) {
            sender.sendMessage(enchantments.prefixed("no-permission"));
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage(enchantments.prefixed("player-only"));
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage("/emchantment <name>");
            return true;
        }

        CustomEnchantment enchantment = enchantments.find(args[0]).orElse(null);
        if (enchantment == null) {
            sender.sendMessage(enchantments.prefixed("unknown-enchantment").replace("%name%", args[0]));
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            sender.sendMessage(enchantments.prefixed("hold-item"));
            return true;
        }
        if (!enchantments.apply(item, enchantment, 1)) {
            sender.sendMessage(enchantments.prefixed("cannot-apply"));
            return true;
        }
        sender.sendMessage(enchantments.prefixed("applied")
                .replace("%display_name%", enchantment.displayName()));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("customenchants.command.emchantment")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            return matchingEnchantments(args[0]);
        }
        return Collections.emptyList();
    }

    private List<String> matchingEnchantments(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (CustomEnchantment enchantment : enchantments.all()) {
            if (enchantment.key().toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(enchantment.key());
            }
        }
        return result;
    }
}
