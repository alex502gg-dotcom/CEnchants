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
            sender.sendMessage("/emchantment <name> [level]");
            return true;
        }

        CustomEnchantment enchantment = enchantments.find(args[0]).orElse(null);
        if (enchantment == null) {
            sender.sendMessage(enchantments.prefixed("unknown-enchantment").replace("%name%", args[0]));
            return true;
        }
        int level = parseLevel(args, enchantment.maxLevel());
        if (level < 1 || level > enchantment.maxLevel()) {
            sender.sendMessage(enchantments.prefixed("invalid-level").replace("%max_level%", String.valueOf(enchantment.maxLevel())));
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            sender.sendMessage(enchantments.prefixed("hold-item"));
            return true;
        }
        if (!enchantments.apply(item, enchantment, level)) {
            sender.sendMessage(enchantments.prefixed("cannot-apply"));
            return true;
        }
        sender.sendMessage(enchantments.prefixed("applied")
                .replace("%display_name%", enchantment.displayName())
                .replace("%level%", EnchantmentManager.toRoman(level)));
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
        if (args.length == 2) {
            return List.of("1", "2", "3");
        }
        return Collections.emptyList();
    }

    private int parseLevel(String[] args, int defaultMax) {
        if (args.length < 2) {
            return 1;
        }
        try {
            return Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored) {
            return defaultMax + 1;
        }
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
