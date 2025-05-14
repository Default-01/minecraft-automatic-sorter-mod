package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

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
    public boolean addItem(ItemStack itemStack) {
        int size = inventory.getContainerSize();
        ItemStack tranferStack = itemStack.copyWithCount(1);
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getItem(i);
            if (this.canCombineStacks(stack, tranferStack) && stack.getCount() < stack.getMaxStackSize()) {
                stack.grow(1);
                inventory.createInventoryUpdatePacket(i);
                return true;
            } else if (stack.isEmpty()) {
                inventory.setItem(i, tranferStack);
                inventory.createInventoryUpdatePacket(i);
                return true;
            }
        }

        return false;
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
