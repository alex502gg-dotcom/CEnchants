package ru.customenchants;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import ru.customenchants.builtin.AutoSmeltEnchantment;
import ru.customenchants.builtin.MagnetismEnchantment;
import ru.customenchants.command.CEnchantmentCommand;
import ru.customenchants.command.EnchantmentCommand;
import ru.customenchants.listener.BlockDropListener;
import ru.customenchants.listener.BookApplyListener;
import ru.customenchants.manager.EnchantmentManager;

public final class CustomEnchantmentsPlugin extends JavaPlugin {
    private EnchantmentManager enchantmentManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        enchantmentManager = new EnchantmentManager(this);
        enchantmentManager.registerBuiltIn(new MagnetismEnchantment(this));
        enchantmentManager.registerBuiltIn(new AutoSmeltEnchantment(this));
        enchantmentManager.load();

        getServer().getPluginManager().registerEvents(new BlockDropListener(this, enchantmentManager), this);
        getServer().getPluginManager().registerEvents(new BookApplyListener(this, enchantmentManager), this);

        EnchantmentCommand enchantmentCommand = new EnchantmentCommand(this, enchantmentManager);
        PluginCommand enchantment = getCommand("emchantment");
        if (enchantment != null) {
            enchantment.setExecutor(enchantmentCommand);
            enchantment.setTabCompleter(enchantmentCommand);
        }

        CEnchantmentCommand cEnchantmentCommand = new CEnchantmentCommand(this, enchantmentManager);
        PluginCommand cenchantment = getCommand("cenchantment");
        if (cenchantment != null) {
            cenchantment.setExecutor(cEnchantmentCommand);
            cenchantment.setTabCompleter(cEnchantmentCommand);
        }
    }

    @Override
    public void onDisable() {
        if (enchantmentManager != null) {
            enchantmentManager.close();
        }
    }

    public EnchantmentManager enchantmentManager() {
        return enchantmentManager;
    }
}
