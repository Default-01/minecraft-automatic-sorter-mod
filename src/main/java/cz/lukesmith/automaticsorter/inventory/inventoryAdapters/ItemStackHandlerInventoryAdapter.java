package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;

public class ItemStackHandlerInventoryAdapter implements IInventoryAdapter {

    private ItemStackHandler itemStackHandler;

    public ItemStackHandlerInventoryAdapter(ItemStackHandler itemStackHandler) {
        this.itemStackHandler = itemStackHandler;
    }

    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (int i = 0; i < this.itemStackHandler.getSlots(); i++) {
            ItemStack stack = this.itemStackHandler.getStackInSlot(i);
            if (this.compareStacks(stack, itemStack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void removeItem(int index, int amount) {
        ItemStack stack = this.itemStackHandler.getStackInSlot(index);
        if (!stack.isEmpty() && stack.getCount() >= amount) {
            stack.shrink(amount);
            this.itemStackHandler.setStackInSlot(index, stack);
        }
    }

    @Override
    public boolean addItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }

        ItemStack transferStack = itemStack.copyWithCount(1);
        for (int i = 0; i < this.itemStackHandler.getSlots(); i++) {
            ItemStack stack = this.itemStackHandler.getStackInSlot(i);
            if (this.canCombineStacks(stack, transferStack) && stack.getCount() < stack.getMaxStackSize()) {
                stack.grow(1);
                this.itemStackHandler.setStackInSlot(i, stack);
                return true;
            } else if (stack.isEmpty()) {
                this.itemStackHandler.setStackInSlot(i, transferStack);
                return true;
            }
        }

        return false;
    }

    @Override
    public ArrayList<ItemStack> getAllStacks() {
        ArrayList<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < this.itemStackHandler.getSlots(); i++) {
            ItemStack stack = this.itemStackHandler.getStackInSlot(i);
            stacks.add(stack);
        }
        return stacks;
    }

    @Override
    public int getSize() {
        return this.itemStackHandler.getSlots();
    }
}
