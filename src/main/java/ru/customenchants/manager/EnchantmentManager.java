package ru.customenchants.manager;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import ru.customenchants.api.CustomEnchantment;
import ru.customenchants.api.CustomEnchantmentProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.jar.JarFile;

public final class EnchantmentManager implements AutoCloseable {
    private final Plugin plugin;
    private final NamespacedKey itemKey;
    private final NamespacedKey bookKey;
    private final NamespacedKey bookLevelKey;
    private final Map<String, CustomEnchantment> enchantments = new LinkedHashMap<>();
    private final Map<String, CustomEnchantment> builtIns = new LinkedHashMap<>();
    private final List<URLClassLoader> externalLoaders = new ArrayList<>();

    public EnchantmentManager(Plugin plugin) {
        this.plugin = plugin;
        this.itemKey = new NamespacedKey(plugin, "custom_enchantments");
        this.bookKey = new NamespacedKey(plugin, "book_enchantment");
        this.bookLevelKey = new NamespacedKey(plugin, "book_level");
    }

    public void registerBuiltIn(CustomEnchantment enchantment) {
        builtIns.put(normalize(enchantment.key()), enchantment);
    }

    public void load() {
        enchantments.clear();
        builtIns.values().forEach(this::registerIfEnabled);

        if (plugin.getConfig().getBoolean("settings.load-external-jars", true)) {
            loadExternalEnchantments();
        }
        plugin.getLogger().info("Loaded " + enchantments.size() + " custom enchantments.");
    }

    public Collection<CustomEnchantment> all() {
        return Collections.unmodifiableCollection(enchantments.values());
    }

    public Optional<CustomEnchantment> find(String key) {
        return Optional.ofNullable(enchantments.get(normalize(key)));
    }

    public int level(ItemStack item, String key) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        String raw = item.getItemMeta().getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        for (String entry : raw.split(";")) {
            String[] parts = entry.split(":", 2);
            if (parts.length >= 1 && normalize(parts[0]).equals(normalize(key))) {
                return 1;
            }
        }
        return 0;
    }

    public boolean apply(ItemStack item, CustomEnchantment enchantment, int ignoredLevel) {
        if (!enchantment.canApply(item)) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        Map<String, Integer> stored = read(meta);
        stored.put(normalize(enchantment.key()), 1);
        write(meta, stored);
        updateLore(meta, stored);
        item.setItemMeta(meta);
        return true;
    }

    public ItemStack createBook(CustomEnchantment enchantment, int ignoredLevel) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta == null) {
            return book;
        }
        String title = color(plugin.getConfig().getString("settings.book-title-format", "&dКнига чара: &f%display_name%")
                .replace("%display_name%", enchantment.displayName()));
        meta.setDisplayName(title);
        List<String> lore = new ArrayList<>();
        lore.add(color("&7" + enchantment.displayName()));
        for (String line : plugin.getConfig().getStringList("settings.book-lore")) {
            lore.add(color(line));
        }
        meta.setLore(lore);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(bookKey, PersistentDataType.STRING, normalize(enchantment.key()));
        pdc.set(bookLevelKey, PersistentDataType.INTEGER, 1);
        book.setItemMeta(meta);
        return book;
    }

    public Optional<BookData> readBook(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return Optional.empty();
        }
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        String key = pdc.get(bookKey, PersistentDataType.STRING);
        if (key == null) {
            return Optional.empty();
        }
        return find(key).map(enchantment -> new BookData(enchantment, 1));
    }

    public String message(String path) {
        return color(plugin.getConfig().getString("settings.messages." + path, path));
    }

    public String prefixed(String path) {
        return message("prefix") + message(path);
    }

    @Override
    public void close() {
        enchantments.values().forEach(CustomEnchantment::onUnregister);
        for (URLClassLoader loader : externalLoaders) {
            try {
                loader.close();
            } catch (IOException ignored) {
            }
        }
        externalLoaders.clear();
        enchantments.clear();
    }

    private void registerIfEnabled(CustomEnchantment enchantment) {
        String key = normalize(enchantment.key());
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("enchantments." + key);
        if (section != null && !section.getBoolean("enabled", true)) {
            return;
        }
        enchantment.onRegister(plugin, section);
        enchantments.put(key, enchantment);
    }

    private void loadExternalEnchantments() {
        File folder = new File(plugin.getDataFolder(), plugin.getConfig().getString("settings.enchantment-folder", "enchantments"));
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Cannot create enchantments folder: " + folder.getAbsolutePath());
            return;
        }
        File[] jars = folder.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".jar"));
        if (jars == null) {
            return;
        }
        for (File jar : jars) {
            loadJar(jar);
        }
    }

    private void loadJar(File jar) {
        try {
            try (JarFile ignored = new JarFile(jar)) {
            }
            URLClassLoader loader = new URLClassLoader(new URL[]{jar.toURI().toURL()}, plugin.getClass().getClassLoader());
            externalLoaders.add(loader);
            ServiceLoader<CustomEnchantmentProvider> serviceLoader = ServiceLoader.load(CustomEnchantmentProvider.class, loader);
            for (CustomEnchantmentProvider provider : serviceLoader) {
                for (CustomEnchantment enchantment : provider.createEnchantments(plugin)) {
                    registerIfEnabled(enchantment);
                }
            }
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to load enchantment jar " + jar.getName() + ": " + exception.getMessage());
        }
    }

    private Map<String, Integer> read(ItemMeta meta) {
        Map<String, Integer> result = new HashMap<>();
        String raw = meta.getPersistentDataContainer().get(itemKey, PersistentDataType.STRING);
        if (raw == null || raw.isBlank()) {
            return result;
        }
        for (String entry : raw.split(";")) {
            String[] parts = entry.split(":", 2);
            if (parts.length >= 1 && !parts[0].isBlank()) {
                result.put(normalize(parts[0]), 1);
            }
        }
        return result;
    }

    private void write(ItemMeta meta, Map<String, Integer> stored) {
        StringBuilder builder = new StringBuilder();
        stored.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    if (!builder.isEmpty()) {
                        builder.append(';');
                    }
                    builder.append(entry.getKey()).append(":1");
                });
        meta.getPersistentDataContainer().set(itemKey, PersistentDataType.STRING, builder.toString());
    }

    private void updateLore(ItemMeta meta, Map<String, Integer> stored) {
        List<String> lore = meta.hasLore() && meta.getLore() != null ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> {
            String stripped = ChatColor.stripColor(line);
            return stripped.startsWith("[CE] ") || enchantments.values().stream()
                    .anyMatch(enchantment -> stripped.equals(enchantment.displayName()));
        });
        stored.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> find(entry.getKey()).ifPresent(enchantment ->
                        lore.add(color("&d" + enchantment.displayName()))));
        meta.setLore(lore);
    }

    public static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }

    public static String toRoman(int number) {
        return String.valueOf(number);
    }

    public record BookData(CustomEnchantment enchantment, int level) {
    }
}
