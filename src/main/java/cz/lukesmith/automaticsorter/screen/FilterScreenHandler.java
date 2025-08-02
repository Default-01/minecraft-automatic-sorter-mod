package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;

public class FilterScreenHandler extends AbstractContainerMenu {

    private final Container inventory;
    public final FilterBlockEntity blockEntity;
    private final ContainerData propertyDelegate;
    private final int inventorySize = 24;


    public FilterScreenHandler(int syncId, Inventory inventory, BlockPos pos) {
        this(syncId, inventory, inventory.player.level().getBlockEntity(pos), new SimpleContainerData(1));
    }

    public FilterScreenHandler(int syncId, Container playerInventory, BlockEntity blockEntity, ContainerData propertyDelegate) {
        super(ModScreenHandlers.FILTER_SCREEN_HANDLER.get(), syncId);
        checkContainerSize((Container) blockEntity, inventorySize);
        this.inventory = ((Container) blockEntity);
        inventory.startOpen(playerInventory.player);
        this.blockEntity = ((FilterBlockEntity) blockEntity);
        this.propertyDelegate = propertyDelegate;

        // Pridat sloty
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 8; j++) {
                this.addSlot(new Slot(this.inventory, j + i * 8, 26 + j * 18, 15 + i * 18));
            }
        }

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addDataSlots(propertyDelegate);
    }


    public @NotNull ItemStack quickMoveStack(Player player, int slot) {
        Slot sourceSlot = this.slots.get(slot);
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack singleItemStack = sourceStack.copy();
        singleItemStack.setCount(1);

        if (slot < 24) {
            if (!this.insertItem(singleItemStack, inventorySize, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!this.insertItem(singleItemStack, 0, inventorySize, false)) {
                return ItemStack.EMPTY;
            }
        }

        sourceStack.decrement(1);

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        return singleItemStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return false;
    }

    @Override
    public boolean canUse(Player player) {
        return this.inventory.stillValid(player);
    }

    private void addPlayerInventory(Container playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Container playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public int getFilterType() {
        return propertyDelegate.get(0);
    }

    public int toggleFilterType() {
        int value = FilterBlockEntity.FilterTypeEnum.nextValue(this.getFilterType());
        propertyDelegate.set(0, value);
        blockEntity.setChanged();
        return value;
    }

    public BlockPos getBlockPos() {
        return blockEntity.getBlockPos();
    }
}
