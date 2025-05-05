package cz.lukesmith.automaticsorter.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class InventoryUtils implements Inventory {

    private final ArrayList<Inventory> inventories = new ArrayList<>();

    /**
     * Getting InventoryUtils from position
     *
     * @param world
     * @param pos
     * @return
     */
    public static InventoryUtils getInventoryUtils(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        InventoryUtils inventoryUtils = new InventoryUtils();
        if (block instanceof ChestBlock chestBlock) {
            inventoryUtils.addInventory(ChestBlock.getInventory(chestBlock, world.getBlockState(pos), world, pos, true));
        } else if (blockEntity instanceof Inventory inventory) {

            // Here add other block entities from mods etc. (Minecraft Barrel is here too)
            InventoryUtils expandedStorageInventory = getExpandedStorageChests(blockEntity, block, world, pos);
            if (!expandedStorageInventory.isEmpty()) {
                inventoryUtils.addInventory(expandedStorageInventory);
            } else {
                inventoryUtils.addInventory(inventory);
            }
        }

        return inventoryUtils;
    }

    // Expanded Storage Mod

    /**
     * Implemented Expanded Storage support
     *
     * @param blockEntity
     * @param block
     * @param world
     * @param pos
     * @return
     */
    private static InventoryUtils getExpandedStorageChests(BlockEntity blockEntity, Block block, World world, BlockPos pos) {
        InventoryUtils inventoryUtils = new InventoryUtils();
        try {
            Class<?> chestBlockClass = Class.forName("compasses.expandedstorage.impl.block.AbstractChestBlock");
            if (chestBlockClass.isInstance(block)) {
                inventoryUtils.addInventory(getExpandedStorageInventory(blockEntity));
                try {
                    Object chestBlock = chestBlockClass.cast(block);
                    Method getContainerMethod = chestBlockClass.getMethod("getDirectionToAttached", BlockState.class);
                    Direction direction = (Direction) getContainerMethod.invoke(chestBlock, world.getBlockState(pos));
                    BlockPos secondPos = pos.offset(direction);
                    BlockEntity secondBlockEntity = world.getBlockEntity(secondPos);
                    Direction facing = world.getBlockState(pos).get(ChestBlock.FACING);
                    if (direction == Direction.UP) {
                        inventoryUtils.addInventoryAsFirst(getExpandedStorageInventory(secondBlockEntity));
                    } else if (
                            (facing == Direction.EAST && direction == Direction.SOUTH)
                                    || (facing == Direction.SOUTH && direction == Direction.WEST)
                                    || (facing == Direction.WEST && direction == Direction.NORTH)
                                    || (facing == Direction.NORTH && direction == Direction.EAST)) {
                        inventoryUtils.addInventoryAsFirst(getExpandedStorageInventory(secondBlockEntity));
                    } else {
                        inventoryUtils.addInventory(getExpandedStorageInventory(secondBlockEntity));
                    }


                } catch (InvocationTargetException ignored) {

                }
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ignored) {

        }

        return inventoryUtils;
    }

    private static Inventory getExpandedStorageInventory(BlockEntity blockEntity) {
        if (blockEntity instanceof Inventory inventory) {
            return inventory;
        }

        return null;
    }

    // Inventory functions

    public void addInventoryAsFirst(Inventory inventory) {
        if (inventory != null) {
            inventories.add(0, inventory);
        }
    }

    public void addInventory(Inventory inventory) {
        if (inventory != null) {
            inventories.add(inventory);
        }
    }

    public ArrayList<Inventory> getInventories() {
        return inventories;
    }

    public void addInventoryUtils(InventoryUtils inventoryUtils) {
        if (inventoryUtils != null) {
            inventories.addAll(inventoryUtils.getInventories());
        }
    }

    @Override
    public int size() {
        return inventories.stream().mapToInt(Inventory::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return inventories.stream().allMatch(Inventory::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        for (Inventory inventory : inventories) {
            if (slot < inventory.size()) {
                return inventory.getStack(slot);
            }
            slot -= inventory.size();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        for (Inventory inventory : inventories) {
            if (slot < inventory.size()) {
                return inventory.removeStack(slot, amount);
            }
            slot -= inventory.size();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStack(int slot) {
        for (Inventory inventory : inventories) {
            if (slot < inventory.size()) {
                return inventory.removeStack(slot);
            }
            slot -= inventory.size();
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        for (Inventory inventory : inventories) {
            if (slot < inventory.size()) {
                inventory.setStack(slot, stack);
                return;
            }
            slot -= inventory.size();
        }
    }

    @Override
    public void markDirty() {
        inventories.forEach(Inventory::markDirty);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {
        inventories.forEach(Inventory::clear);
    }
}