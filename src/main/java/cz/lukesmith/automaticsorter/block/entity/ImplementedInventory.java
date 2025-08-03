package cz.lukesmith.automaticsorter.block.entity;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.List;


public interface ImplementedInventory extends Container {
    /**
     * Gets the item list of this inventory.
     * Must return the same instance every time it's called.
     *
     * @return the item list
     */
    NonNullList<ItemStack> getItems();


    static ImplementedInventory of(NonNullList<ItemStack> items) {
        return () -> items;
    }

    default int getContainerSize() {
        return getItems().size();
    }

    // Inventory

    /**
     * Returns the inventory size.
     *
     * <p>The default implementation returns the size of {@link #getItems()}.
     *
     * @return the inventory size
     */
    default int size() {
        return getItems().size();
    }

    /**
     * @return true if this inventory has only empty stacks, false otherwise
     */
    @Override
    default boolean isEmpty() {
        for (int i = 0; i < size(); i++) {
            ItemStack stack = getStack(i);
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    default ItemStack getStack(int slot) {
        return getItems().get(slot);
    }
}