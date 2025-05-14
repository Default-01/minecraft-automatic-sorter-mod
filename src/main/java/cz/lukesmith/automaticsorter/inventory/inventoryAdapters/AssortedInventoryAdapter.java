package cz.lukesmith.automaticsorter.inventory.inventoryAdapters;

import cz.lukesmith.automaticsorter.inventory.inventoryUtils.AssortedInventoryUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class AssortedInventoryAdapter implements IInventoryAdapter {

    private final ArrayList<ItemStack> itemStacks;

    private final BlockEntity blockEntity;

    public AssortedInventoryAdapter(ArrayList<ItemStack> itemStacks, BlockEntity blockEntity) {
        this.itemStacks = itemStacks;
        this.blockEntity = blockEntity;
    }

    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (ItemStack stack : itemStacks) {
            if (compareStacks(stack, itemStack)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void removeItem(int index, int amount) {
        extractItem(index, amount);
    }

    @Override
    public boolean addItem(ItemStack itemStack) {
        int size = itemStacks.size();
        ItemStack tranferStack = itemStack.copyWithCount(1);
        for (int i = 0; i < size; i++) {
            ItemStack stack = itemStacks.get(i);
            if (canCombineStacks(stack, tranferStack) && stack.getCount() < stack.getMaxStackSize()) {
                insertItem(i, tranferStack);
                return true;
            } else if (stack.isEmpty()) {
                insertItem(i, tranferStack);
                return true;
            }
        }

        return false;
    }

    @Override
    public ArrayList<ItemStack> getAllStacks() {
        return itemStacks;
    }

    @Override
    public int getSize() {
        return itemStacks.size();
    }

    private void extractItem(int index, int amount) {
        try {
            Object storageHandler = AssortedInventoryUtil.getAssortedStorageItemStackStorageHandler(this.blockEntity);
            Class<?> itemStackStorageHandlerClass = Class.forName(AssortedInventoryUtil.STORAGE_HANDLER_CLASSNAME);
            Method insertItemMethod = itemStackStorageHandlerClass.getDeclaredMethod("extractItem", int.class, int.class, boolean.class);
            insertItemMethod.invoke(storageHandler, index, amount, false);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertItem(int index, ItemStack itemstack) {
        try {
            Object storageHandler = AssortedInventoryUtil.getAssortedStorageItemStackStorageHandler(this.blockEntity);
            Class<?> itemStackStorageHandlerClass = Class.forName(AssortedInventoryUtil.STORAGE_HANDLER_CLASSNAME);
            Method insertItemMethod = itemStackStorageHandlerClass.getDeclaredMethod("insertItem", int.class, ItemStack.class, boolean.class);
            insertItemMethod.invoke(storageHandler, index, itemstack, false);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
