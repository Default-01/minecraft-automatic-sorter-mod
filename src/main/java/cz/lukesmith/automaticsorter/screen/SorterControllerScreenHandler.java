package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.block.ModBlocks;
import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import cz.lukesmith.automaticsorter.block.entity.SorterControllerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

public class SorterControllerScreenHandler extends AbstractContainerMenu {

    private final SorterControllerBlockEntity blockEntity;
    private final Level level;

    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int FILTER_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int FILTER_INVENTORY_SLOT_COUNT = 1;


    public SorterControllerScreenHandler(int syncId, Inventory inventory, FriendlyByteBuf extraData) {
        this(syncId, inventory, inventory.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public SorterControllerScreenHandler(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModScreenHandlers.SORTER_CONTROLLER_SCREEN_HANDLER.get(), containerId);

        if (!(blockEntity instanceof SorterControllerBlockEntity sorterControllerBlockEntity))
            throw new IllegalStateException("Invalid block entity at container open");


        this.blockEntity = sorterControllerBlockEntity;
        this.level = playerInventory.player.level();

        ItemStackHandler inventory = this.blockEntity.getInventory();

        this.addSlot(new SlotItemHandler(inventory, 0, 80, 33));

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copy = sourceStack.copy();

        if (index < VANILLA_SLOT_COUNT) {
            // Z hráče do blockEntity
            if (!moveItemStackTo(sourceStack, FILTER_INVENTORY_FIRST_SLOT_INDEX, FILTER_INVENTORY_FIRST_SLOT_INDEX + FILTER_INVENTORY_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < FILTER_INVENTORY_FIRST_SLOT_INDEX + FILTER_INVENTORY_SLOT_COUNT) {
            // Z blockEntity do hráče
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        sourceSlot.onTake(player, sourceStack);
        return copy;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, ModBlocks.FILTER_BLOCK.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    public String getSpeedBoostText() {
        double speed = blockEntity.getSpeedPerSecond();
        if (speed == Double.MAX_VALUE) {
            return "∞";
        }

        return String.format("%.2f", speed);
    }
}
