package me.elsiff.luckyrecipe;

import me.elsiff.luckyrecipe.util.IdentityUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.Repairable;

import java.io.File;
import java.util.*;

public class RecipeManager {
    private final LuckyRecipe plugin;
    private final Map<Recipe, CustomRecipe> recipeMap = new HashMap<Recipe, CustomRecipe>();

    public RecipeManager(LuckyRecipe plugin) {
        this.plugin = plugin;
        this.loadRecipeList();
    }

    public void loadRecipeList() {
        File folder = new File(plugin.getDataFolder(), "recipes");

        if (!folder.exists()) {
            if (!plugin.getDataFolder().mkdir() || !folder.mkdir()) {
                return;
            }

            plugin.saveResource("recipes/defaultPickaxe.yml", false);
            plugin.saveResource("recipes/defaultLeatherArmor.yml", false);
        }

        for (File file : folder.listFiles()) {
            if (!file.getName().endsWith(".yml"))
                continue;

            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            for (String path : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(path + ".result-list");
                CustomRecipe.Builder builder = CustomRecipe.builder();

                for (String name : section.getKeys(false)) {
                    double chance = section.getDouble(name + ".chance");

                    Material type = IdentityUtils.getMaterial(section.getString(name + ".id"));
                    int amount = section.getInt(name + ".amount");
                    short durability = (short) ((section.contains(name + ".durability")) ? section.getInt(name + ".durability") : 0);
                    ItemStack stack = new ItemStack(type, amount, durability);
                    ItemMeta meta = stack.getItemMeta();

                    if (section.contains(name + ".display-name")) {
                        meta.setDisplayName(section.getString(name + ".display-name"));
                    }

                    if (section.contains(name + ".lore")) {
                        List<String> lore = new ArrayList<String>();
                        lore.addAll(section.getStringList(name + ".lore"));

                        ListIterator<String> it = lore.listIterator();
                        while (it.hasNext()) {
                            it.set(ChatColor.translateAlternateColorCodes('&', it.next()));
                        }

                        meta.setLore(lore);
                    }

                    if (section.contains(name + ".enchantments")) {
                        for (String content : section.getStringList(name + ".enchantments")) {
                            String[] split = content.split("\\|");
                            Enchantment enchantment = IdentityUtils.getEnchantment(split[0]);
                            int level = Integer.parseInt(split[1]);

                            meta.addEnchant(enchantment, level, true);
                        }
                    }

                    if (section.contains(name + ".hide-flags")) {
                        for (String content : section.getStringList(name + ".hide-flags")) {
                            ItemFlag flag = ItemFlag.valueOf(content.toUpperCase());

                            meta.addItemFlags(flag);
                        }
                    }

                    if (meta instanceof LeatherArmorMeta && section.contains(name + ".color")) {
                        String hex = section.getString(name + ".color");
                        int r = Integer.valueOf(hex.substring(1, 3), 16);
                        int g = Integer.valueOf(hex.substring(3, 5), 16);
                        int b = Integer.valueOf(hex.substring(5, 7), 16);
                        Color color = Color.fromRGB(r, g, b);

                        LeatherArmorMeta lam = (LeatherArmorMeta) meta;
                        lam.setColor(color);
                    }

                    stack.setItemMeta(meta);

                    builder.addResult(stack, chance);
                }

                CustomRecipe recipe = builder.build();

                Material type = IdentityUtils.getMaterial(config.getString(path + ".recipe.id"));
                int amount = config.getInt(path + ".recipe.amount");
                short durability = (short) ((config.contains(path + ".recipe.durability")) ? config.getInt(path + ".recipe.durability") : -1);

                for (Recipe key : findRecipes(type, amount, durability)) {
                    recipeMap.put(key, recipe);
                }
            }
        }

        plugin.getLogger().info("Loaded " + recipeMap.keySet().size() + " recipes!");
    }

    private Set<Recipe> findRecipes(Material type, int amount, short durability) {
        Set<Recipe> set = new HashSet<Recipe>();

        Iterator<Recipe> it = plugin.getServer().recipeIterator();
        while (it.hasNext()) {
            Recipe recipe = it.next();

            if (recipe.getResult().getType() != type) {
                continue;
            }

            if (recipe.getResult().getAmount() != amount) {
                continue;
            }

            if (durability != -1 && recipe.getResult().getDurability() != durability) {
                continue;
            }

            set.add(recipe);
        }

        return set;
    }

    public ItemStack generateResult(Recipe origin) {
        if (origin instanceof ShapelessRecipe && isRepair((ShapelessRecipe) origin)) {
            return origin.getResult();
        }

        CustomRecipe recipe = plugin.getRecipeManager().getRecipe(origin);
        ItemStack result = recipe.getSpecialResult();

        if (result == null) {
            result = origin.getResult();
        }

        return result;
    }

    private boolean isRepair(ShapelessRecipe recipe) {
        if (recipe.getIngredientList().size() != 2 || !(recipe.getResult().getItemMeta() instanceof Repairable)) {
            return false;
        }

        for (ItemStack ingredient : recipe.getIngredientList()) {
            if (ingredient.getType() != recipe.getResult().getType()) {
                return false;
            }
        }

        return true;
    }

    public CustomRecipe getRecipe(Recipe recipe) {
        for (Recipe key : recipeMap.keySet()) {
            if (key.getResult().equals(recipe.getResult())) {
                return recipeMap.get(key);
            }
        }

        return null;
    }

    public boolean containsRecipe(Recipe recipe) {
        for (Recipe key : recipeMap.keySet()) {
            if (key.getResult().equals(recipe.getResult())) {
                return true;
            }
        }

        return false;
    }
}
