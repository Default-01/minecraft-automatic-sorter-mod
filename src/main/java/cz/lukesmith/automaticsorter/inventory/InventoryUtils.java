package cz.lukesmith.automaticsorter.inventory;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class InventoryUtils {

    @NotNull
    public static IInventoryAdapter getInventoryAdapter(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (block instanceof ChestBlock chestBlock) {
            return new InventoryAdapter(ChestBlock.getInventory(chestBlock, world.getBlockState(pos), world, pos, true));
        } else if (blockEntity instanceof Inventory inventory) {
            if (isExpandedStorageChest(block)) {
                return getExpandedStorageInventoryAdapter(world, pos, block, blockEntity);
            } else {
                return new InventoryAdapter(inventory);
            }
        } else {
            if (isAssortedStorageChest(blockEntity)) {
                return getAssortedStorageInventoryAdapter(blockEntity);
            }
        }

        return new NoInventoryAdapter();
    }

    // Assorted Storage Mod

    private static String getAssortedStorageClassName() {
        return "com.grim3212.assorted.storage.common.block.blockentity.BaseStorageBlockEntity";
    }

    public static String getAssortedStorageItemStackStorageHandlerClassName() {
        return "com.grim3212.assorted.lib.core.inventory.impl.ItemStackStorageHandler";
    }

    private static boolean isAssortedStorageChest(BlockEntity blockEntity) {
        try {
            Class<?> chestBlockClass = Class.forName(getAssortedStorageClassName());
            return chestBlockClass.isInstance(blockEntity);
        } catch (ClassNotFoundException ignored) {

        }

        return false;
    }

    public static Object getAssortedStorageItemStackStorageHandler(BlockEntity blockEntity) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> chestBlockClass = Class.forName(getAssortedStorageClassName());
        if (chestBlockClass.isInstance(blockEntity)) {
            Object chestBlockEntity = chestBlockClass.cast(blockEntity);
            Method isLockedMethod = chestBlockClass.getMethod("isLocked");
            boolean isLocked = (boolean) isLockedMethod.invoke(chestBlockEntity);
            if (isLocked) {
                return null;
            }

            Method getContainerMethod = chestBlockClass.getMethod("getStorageHandler");
            Object lockedItemStackStorageHandler = getContainerMethod.invoke(chestBlockEntity);
            Class<?> itemStackStorageHandlerClass = Class.forName(getAssortedStorageItemStackStorageHandlerClassName());
            Class<?> fabricPlatformInvClass = Class.forName("com.grim3212.assorted.lib.inventory.FabricPlatformInventoryStorageHandlerUnsided");
            if (fabricPlatformInvClass.isInstance(lockedItemStackStorageHandler)) {
                Object fabricPlatformInventory = fabricPlatformInvClass.cast(lockedItemStackStorageHandler);
                Method getItemStorageHandlerMethod = fabricPlatformInvClass.getDeclaredMethod("getItemStorageHandler", Direction.class);
                Object storageHandler = getItemStorageHandlerMethod.invoke(fabricPlatformInventory, (Direction) null);

                return itemStackStorageHandlerClass.cast(storageHandler);
            }
        }

        throw new ClassNotFoundException("Class not found: " + getAssortedStorageItemStackStorageHandlerClassName());
    }

    private static IInventoryAdapter getAssortedStorageInventoryAdapter(BlockEntity blockEntity) {
        try {
            Class<?> itemStackStorageHandlerClass = Class.forName(getAssortedStorageItemStackStorageHandlerClassName());
            Object itemStackStorageHandler = getAssortedStorageItemStackStorageHandler(blockEntity);
            if (itemStackStorageHandler == null) {
                return new NoInventoryAdapter();
            }
            Method getSlotsMethod = itemStackStorageHandlerClass.getMethod("getSlots");
            int slots = (int) getSlotsMethod.invoke(itemStackStorageHandler);
            Method getStackMethod = itemStackStorageHandlerClass.getMethod("getStackInSlot", int.class);
            ArrayList<ItemStack> itemStacks = new ArrayList<>();
            for (int i = 0; i < slots; i++) {
                ItemStack itemStack = (ItemStack) getStackMethod.invoke(itemStackStorageHandler, i);
                itemStacks.add(itemStack);
            }

            return new AssortedInventoryAdapter(itemStacks, blockEntity);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException ignored) {

        }

        return new NoInventoryAdapter();
    }

    // Expanded Storage Mod

    private static String getExpandedStorageClassName() {
        return "compasses.expandedstorage.impl.block.AbstractChestBlock";
    }

    private static boolean isExpandedStorageChest(Block block) {
        try {
            Class<?> chestBlockClass = Class.forName(getExpandedStorageClassName());
            return chestBlockClass.isInstance(block);
        } catch (ClassNotFoundException ignored) {

        }

        return false;
    }

    private static IInventoryAdapter getExpandedStorageInventoryAdapter(World world, BlockPos pos, Block block, BlockEntity blockEntity) {
        try {
            Class<?> chestBlockClass = Class.forName(getExpandedStorageClassName());
            if (chestBlockClass.isInstance(block)) {
                MultiInventoryAdapter multiInventoryAdapter = new MultiInventoryAdapter(new InventoryAdapter(getExpandedStorageInventory(blockEntity)));
                try {
                    Object chestBlock = chestBlockClass.cast(block);
                    Method getContainerMethod = chestBlockClass.getMethod("getDirectionToAttached", BlockState.class);
                    Direction direction = (Direction) getContainerMethod.invoke(chestBlock, world.getBlockState(pos));
                    BlockPos secondPos = pos.offset(direction);
                    BlockEntity secondBlockEntity = world.getBlockEntity(secondPos);
                    Direction facing = world.getBlockState(pos).get(ChestBlock.FACING);
                    InventoryAdapter secondInventoryAdapter = new InventoryAdapter(getExpandedStorageInventory(secondBlockEntity));
                    if ((facing == Direction.EAST && direction == Direction.SOUTH)
                            || (facing == Direction.SOUTH && direction == Direction.WEST)
                            || (facing == Direction.WEST && direction == Direction.NORTH)
                            || (facing == Direction.NORTH && direction == Direction.EAST)
                            || (direction == Direction.UP)) {
                        multiInventoryAdapter.addInventoryAdapterAsFirst(secondInventoryAdapter);
                    } else {
                        multiInventoryAdapter.addInventoryAdapter(secondInventoryAdapter);
                    }

                    return multiInventoryAdapter;
                } catch (InvocationTargetException ignored) {

                }

                return multiInventoryAdapter;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException ignored) {

        }

        return new NoInventoryAdapter();
    }

    private static Inventory getExpandedStorageInventory(BlockEntity blockEntity) {
        try {
            Class<?> chestBlockEntityClass = Class.forName("compasses.expandedstorage.impl.block.entity.ChestBlockEntity");
            if (chestBlockEntityClass.isInstance(blockEntity)) {
                Object chestBlockEntity = chestBlockEntityClass.cast(blockEntity);
                Method getInventory = chestBlockEntityClass.getMethod("getInventory");
                if (getInventory.invoke(chestBlockEntity) instanceof Inventory inventory) {
                    return inventory;
                }
            }
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException ignored) {

        }

        return null;
    }
}