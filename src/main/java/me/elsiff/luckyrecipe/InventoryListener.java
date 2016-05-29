package me.elsiff.luckyrecipe;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.Repairable;

public class InventoryListener implements Listener {
    private final int[] slots;
    private final LuckyRecipe plugin;

    public InventoryListener(LuckyRecipe plugin) {
        this.plugin = plugin;
        this.slots = new int[36];

        for (int i = 0; i < 9; i ++) {
            slots[i] = (8 - i);
            slots[i + 9] = (8 - i) + 27;
            slots[i + 18] = (8 - i) + 18;
            slots[i + 27] = (8 - i) + 9;
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (plugin.getRecipeManager().containsRecipe(event.getRecipe())) {
            if (event.isShiftClick()) {
                event.setCancelled(true);

                PlayerInventory inv = event.getWhoClicked().getInventory();
                int craftable = getCraftableAmount(event.getInventory());
                int receptible = getReceptibleAmount(inv, event.getRecipe().getResult());

                if (craftable == 0 || receptible == 0) {
                    return;
                }

                if (craftable > receptible) {
                    craftable = receptible;
                }

                for (int i = 0; i < craftable; i ++) {
                    ItemStack stack = plugin.getRecipeManager().generateResult(event.getRecipe());

                    addItem(inv, stack);
                }

                updateMatrix(event.getInventory(), craftable);

            } else {
                ItemStack result = plugin.getRecipeManager().generateResult(event.getRecipe());

                if (event.getCursor() != null && event.getCursor().getType() != Material.AIR && !event.getCursor().isSimilar(result)) {
                    event.setCancelled(true);

                    addItem(event.getWhoClicked().getInventory(), result);
                    updateMatrix(event.getInventory(), 1);
                } else {
                    event.setCurrentItem(result);
                }
            }
        }
    }

    private void updateMatrix(CraftingInventory inv, int amount) {
        ItemStack[] matrix = inv.getMatrix();

        for (int i = 0; i < matrix.length; i ++) {
            ItemStack ingredient = matrix[i];

            if (ingredient != null && ingredient.getType() != Material.AIR) {

                if (ingredient.getAmount() == amount) {
                    matrix[i] = null;
                } else {
                    ingredient.setAmount(ingredient.getAmount() - amount);
                }

            }
        }

        inv.setMatrix(matrix);
    }

    private int getCraftableAmount(CraftingInventory inv) {
        int amount = 64;

        for (ItemStack stack : inv.getMatrix()) {
            if (stack == null || stack.getType() == Material.AIR)
                continue;

            if (amount > stack.getAmount()) {
                amount = stack.getAmount();
            }
        }

        return amount;
    }

    private int getReceptibleAmount(PlayerInventory inv, ItemStack item) {
        int amount = 0;

        for (int slot = 0; slot < 36; slot ++) {
            ItemStack stack = inv.getItem(slot);

            if (stack == null || stack.getType() == Material.AIR) {
                amount += item.getMaxStackSize();
            }

            if (stack != null && stack.isSimilar(item) && item.getMaxStackSize() > stack.getAmount()) {
                amount += (item.getMaxStackSize() - stack.getAmount());
            }
        }

        return amount;
    }

    private void addItem(PlayerInventory inv, ItemStack stack) {
        ItemStack[] contents = inv.getStorageContents();
        boolean hasAdded = false;

        for (int slot : slots) {
            ItemStack slotStack = contents[slot];

            if (slotStack == null || slotStack.getType() == Material.AIR) {
                contents[slot] = stack;
                hasAdded = true;
                break;
            }

            if (slotStack != null && slotStack.isSimilar(stack) && slotStack.getAmount() < slotStack.getMaxStackSize()) {
                int newAmount = slotStack.getAmount() + stack.getAmount();

                if (newAmount > stack.getMaxStackSize()) {
                    newAmount -= stack.getMaxStackSize();

                    ItemStack newStack = stack.clone();
                    newStack.setAmount(stack.getMaxStackSize());

                    addItem(inv, newStack);
                }

                slotStack.setAmount(newAmount);
                contents[slot] = slotStack;
                hasAdded = true;
                break;
            }
        }

        if (!hasAdded) {
            inv.getHolder().getWorld().dropItem(inv.getHolder().getLocation(), stack);
        }

        inv.setStorageContents(contents);
    }
}
