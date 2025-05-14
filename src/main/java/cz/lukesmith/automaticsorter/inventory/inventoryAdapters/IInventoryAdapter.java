package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public interface IInventoryAdapter {

    ItemStack containsItem(ItemStack itemStack);

    void removeItem(int index, int amount);

    boolean addItem(ItemStack itemStack);

    ArrayList<ItemStack> getAllStacks();

    int getSize();

    default boolean isEmpty() {
        return getAllStacks().isEmpty();
    }

    default boolean compareStacks(ItemStack insertingItem, ItemStack compareItem) {
        if (insertingItem.isEnchantable()) {
            return ItemStack.isSameItem(insertingItem, compareItem);
        }

        return ItemStack.isSameItemSameComponents(insertingItem, compareItem);
    }

    default boolean canCombineStacks(ItemStack insertingItem, ItemStack compareItem) {
        return ItemStack.isSameItemSameComponents(insertingItem, compareItem);
    }
}
