package me.elsiff.luckyrecipe;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CustomRecipe {
    private final Map<ItemStack, Double> resultMap;

    public CustomRecipe(Map<ItemStack, Double> resultMap) {
        this.resultMap = resultMap;
    }

    public ItemStack getSpecialResult() {
        double currentVar = 0.0D;
        double randomVar = Math.random();

        for (ItemStack stack : resultMap.keySet()) {
            currentVar += resultMap.get(stack);

            if (randomVar <= currentVar) {
                return stack;
            }
        }

        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<ItemStack, Double> resultMap = new HashMap<ItemStack, Double>();

        public Builder addResult(ItemStack stack, double chance) {
            this.resultMap.put(stack, 0.01D * chance);
            return this;
        }

        public CustomRecipe build() {
            return new CustomRecipe(resultMap);
        }
    }
}
