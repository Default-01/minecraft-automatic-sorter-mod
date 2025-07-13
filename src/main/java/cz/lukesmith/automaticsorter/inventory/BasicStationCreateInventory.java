package cz.lukesmith.automaticsorter.inventory;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class BasicStationCreateInventory implements IInventoryAdapter {
    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        return null;
    }

    @Override
    public void removeItem(int index, int amount) {

    }

    @Override
    public boolean addItem(ItemStack itemStack) {
        return false;
    }

    @Override
    public ArrayList<ItemStack> getAllStacks() {
        return null;
    }

    @Override
    public int getSize() {
        return 0;
    }
}
