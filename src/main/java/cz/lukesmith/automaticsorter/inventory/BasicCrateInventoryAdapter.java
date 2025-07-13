package cz.lukesmith.automaticsorter.inventory;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static cz.lukesmith.automaticsorter.inventory.InventoryUtils.getBasicStorageCrateSlotClassName;

public class BasicCrateInventoryAdapter implements IInventoryAdapter {

    private Object crateSlot;
    private Object component;

    public BasicCrateInventoryAdapter(Object crateSlot) {
        this.crateSlot = crateSlot;
        this.component = getComponent();
    }

    @Override
    public ItemStack containsItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        if (this.isComponentInicialized()) {
            try {
                Class<?> componentClass = this.component.getClass();
                Method itemMethod = componentClass.getMethod("item");
                Object itemComponent = itemMethod.invoke(this.component);
                if (itemComponent instanceof Item) {
                    Item item = (Item) itemComponent;
                    ItemStack itemStackToCheck = new ItemStack(item, itemStack.getCount());
                    if (ItemStack.areItemsAndComponentsEqual(itemStack, itemStackToCheck)) {
                        return itemStack;
                    }
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            }
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void removeItem(int index, int amount) {
        if (this.isComponentInicialized()) {
            try (Transaction transaction = Transaction.openOuter()) {
                Class<?> componentClass = this.component.getClass();
                Method itemMethod = componentClass.getMethod("item");
                Object itemComponent = itemMethod.invoke(this.component);
                if (itemComponent instanceof Item) {
                    Item item = (Item) itemComponent;
                    ItemVariant itemVariant = ItemVariant.of(item);
                    Method extractMethod = this.crateSlot.getClass().getMethod("extract", ItemVariant.class, long.class, Transaction.class);
                    extractMethod.invoke(this.crateSlot, itemVariant, amount, transaction);
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            }
        }
        /*
                int size = inventory.size();
        ItemStack tranferStack = itemStack.copyWithCount(1);
        for (int i = 0; i < size; i++) {
            ItemStack stack = inventory.getStack(i);
            if (ItemStack.areItemsAndComponentsEqual(stack, tranferStack) && stack.getCount() < stack.getMaxCount()) {
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
         */
    }

    @Override
    public boolean addItem(ItemStack itemStack) {
        if (this.isComponentInicialized()) {
            try {
                Class<?> componentClass = this.component.getClass();
                Method itemMethod = componentClass.getMethod("item");
                Object itemComponent = itemMethod.invoke(this.component);
                if (itemComponent instanceof Item) {
                    Item item = (Item) itemComponent;
                    ItemVariant itemVariant = ItemVariant.of(item);
                    try (Transaction transaction = Transaction.openOuter()) {
                        Method insertMethod = this.crateSlot.getClass().getMethod("insert", ItemVariant.class, long.class, Transaction.class);
                        long insertedAmount = (long) insertMethod.invoke(this.crateSlot, itemVariant, 1, transaction);
                        if (insertedAmount > 0) {
                            return true;
                        }
                    }
                }
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {
            }
        }

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

    private Object getComponent() {
        try {
            Class<?> basicStorageChestClass = Class.forName(getBasicStorageCrateSlotClassName());
            if (basicStorageChestClass.isInstance(this.crateSlot)) {
                Object basicStorageChest = basicStorageChestClass.cast(this.crateSlot);
                Method getComponentMethod = basicStorageChestClass.getMethod("toComponent");
                return getComponentMethod.invoke(basicStorageChest);
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

        }

        return null;
    }

    private boolean isComponentInicialized() {
        return this.component != null;
    }
}
