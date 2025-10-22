package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class InventoryAdapter implements IInventoryAdapter {

    private final Container inventory;

    public InventoryAdapter(Container inventory) {
        this.inventory = inventory;
    }

    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int size = inventory.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getItem(i);
            if (this.compareStacks(stack, itemStack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void removeItem(int index, int amount) {
        inventory.removeItem(index, amount);
    }

    @Override
    public int addItem(ItemStack itemStack, int maxAmount) {
        int size = inventory.getContainerSize();
        int toTransfer = Math.min(maxAmount, itemStack.getCount());
        int transferred = 0;
        for (int i = 0; i < size && transferred < toTransfer; i++) {
            ItemStack stack = inventory.getItem(i);
            if (this.canCombineStacks(stack, itemStack) && stack.getCount() < stack.getMaxStackSize()) {
                int canAdd = Math.min(toTransfer - transferred, stack.getMaxStackSize() - stack.getCount());
                stack.grow(canAdd);
                transferred += canAdd;
                inventory.setChanged();
            } else if (stack.isEmpty()) {
                int put = toTransfer - transferred;
                ItemStack newStack = itemStack.copyWithCount(put);
                inventory.setItem(i, newStack);
                transferred += put;
                inventory.setChanged();
            }
        }
        return transferred;
    }

    @Override
    public ArrayList<ItemStack> getAllStacks() {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        int size = inventory.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getItem(i);
            stacks.add(stack);
        }

        return stacks;
    }

    @Override
    public int getSize() {
        return inventory.getContainerSize();
    }
}
