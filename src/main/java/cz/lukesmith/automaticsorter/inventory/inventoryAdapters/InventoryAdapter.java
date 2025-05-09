package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class InventoryAdapter implements IInventoryAdapter {

    private final Inventory inventory;

    public InventoryAdapter(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int size = inventory.size();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getStack(i);
            if (this.compareStacks(stack, itemStack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void removeItem(int index, int amount) {
        inventory.removeStack(index, amount);
    }

    @Override
    public boolean addItem(ItemStack itemStack) {
        int size = inventory.size();
        ItemStack tranferStack = itemStack.copyWithCount(1);
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getStack(i);
            if (this.canCombineStacks(stack, tranferStack) && stack.getCount() < stack.getMaxCount()) {
                stack.increment(1);
                inventory.markDirty();
                return true;
            } else if (stack.isEmpty()) {
                inventory.setStack(i, tranferStack);
                inventory.markDirty();
                return true;
            }
        }

        return false;
    }

    @Override
    public ArrayList<ItemStack> getAllStacks() {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        int size = inventory.size();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getStack(i);
            stacks.add(stack);
        }

        return stacks;
    }

    @Override
    public int getSize() {
        return inventory.size();
    }
}
