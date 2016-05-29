package me.elsiff.luckyrecipe;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LuckyRecipe extends JavaPlugin {
    private RecipeManager recipeManager;

    @Override
    public void onEnable() {
        this.recipeManager = new RecipeManager(this);

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new InventoryListener(this), this);

        getLogger().info("Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin has been disabled!");
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }
}
