package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.block.custom.FilterBlock;
import cz.lukesmith.automaticsorter.block.custom.PipeBlock;
import cz.lukesmith.automaticsorter.util.InventoryUtils;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class SorterControllerBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {

    private int ticker = 0;
    private static final int MAX_TICKER = 5;

    public SorterControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SORTER_CONTROLLER_BLOCK_ENTITY, pos, state);
    }

    public static SorterControllerBlockEntity create(BlockPos pos, BlockState state) {
        return new SorterControllerBlockEntity(pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return DefaultedList.ofSize(1, ItemStack.EMPTY);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf packetByteBuf) {

    }

    @Override
    public Text getDisplayName() {
        return null;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return null;
    }

    public static void tick(World world, BlockPos pos, BlockState state, SorterControllerBlockEntity e) {
        if (world.isClient) {
            return;
        }

        if (e.ticker > 0) {
            e.ticker--;
            return;
        }

        Set<BlockPos> connectedPipes = findConnectedPipes(world, pos);
        Set<BlockPos> connectedFilters = findConnectedFilters(world, connectedPipes);

        BlockPos rootChestPos = pos.up();

        InventoryUtils rootChestInventory = InventoryUtils.getInventoryUtils(world, rootChestPos);
        if (!rootChestInventory.getInventories().isEmpty()) {
            Set<BlockPos> noFilterBlockPos = new HashSet<>();
            boolean itemTransfered = false;
            for (BlockPos filterPos : connectedFilters) {
                Direction filterDirection = world.getBlockState(filterPos).get(FilterBlock.FACING);
                BlockPos chestPos = filterPos.offset(filterDirection);
                InventoryUtils inventoryUtils = InventoryUtils.getInventoryUtils(world, chestPos);
                if (!inventoryUtils.getInventories().isEmpty()) {
                    BlockEntity filterEntity = world.getBlockEntity(filterPos);
                    if (filterEntity instanceof FilterBlockEntity filterBlockEntity) {
                        int filterType = filterBlockEntity.getFilterType();
                        itemTransfered = false;
                        switch (FilterBlockEntity.FilterTypeEnum.fromValue(filterType)) {
                            case WHITELIST:
                                itemTransfered = transferWhitelistItem(rootChestInventory, inventoryUtils, filterBlockEntity);
                                break;
                            case IN_INVENTORY:
                                itemTransfered = transferCommonItem(rootChestInventory, inventoryUtils);
                                break;
                            case REJECTS:
                                itemTransfered = false;
                                noFilterBlockPos.add(filterPos);
                                break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + FilterBlockEntity.FilterTypeEnum.fromValue(filterType));
                        }

                        if (itemTransfered) {
                            break;
                        }
                    }
                }
            }

            if (!noFilterBlockPos.isEmpty() && !itemTransfered) {
                transferRejectedItem(world, noFilterBlockPos, rootChestInventory);
            }
        }

        e.ticker = MAX_TICKER;
    }

    /**
     * Transfers an item from one inventory to another if the item is present in the whitelist of the filter.
     *
     * @param from
     * @param to
     * @param filterBlockEntity
     * @return
     */
    private static boolean transferWhitelistItem(Inventory from, Inventory to, FilterBlockEntity filterBlockEntity) {
        for (int i = 0; i < from.size(); i++) {
            ItemStack stack = from.getStack(i);
            if (!stack.isEmpty()) {
                ItemStack singleItem = stack.split(1); // Remove one item from the current stack
                if (filterBlockEntity.isItemInInventory(singleItem)) {
                    ItemStack remaining = transferItem(to, singleItem);

                    if (remaining.isEmpty()) {
                        from.setStack(i, stack);
                        return true; // Item was successfully transferred
                    } else {
                        stack.increment(1); // Revert the split if the item was not transferred
                        from.setStack(i, stack);
                    }
                } else {
                    stack.increment(1); // Revert the split if the item is not in the whitelist
                    from.setStack(i, stack);
                }
            }
        }

        return false;
    }

    /**
     * Transfers an item from one inventory to another if the item is present in the target inventory.
     *
     * @param from
     * @param to
     * @return
     */
    private static boolean transferCommonItem(Inventory from, Inventory to) {
        for (int i = 0; i < from.size(); i++) {
            ItemStack stack = from.getStack(i);
            if (!stack.isEmpty() && containsItem(to, stack)) {
                ItemStack singleItem = stack.split(1); // Remove one item from the current stack
                ItemStack remaining = transferItem(to, singleItem);

                if (remaining.isEmpty()) {
                    from.setStack(i, stack);
                    return true; // Item was successfully transferred
                } else {
                    stack.increment(1); // Revert the split if the item was not transferred
                    from.setStack(i, stack);
                }
            }
        }
        return false;
    }

    private static boolean transferRejectedItem(World world, Set<BlockPos> noFilterBlockPos, Inventory rootChestInventory) {
        for (BlockPos filterPos : noFilterBlockPos) {
            Direction filterDirection = world.getBlockState(filterPos).get(FilterBlock.FACING);
            BlockPos chestPos = filterPos.offset(filterDirection);
            InventoryUtils inventoryUtils = InventoryUtils.getInventoryUtils(world, chestPos);
            if (!inventoryUtils.getInventories().isEmpty()) {
                for (int i = 0; i < rootChestInventory.size(); i++) {
                    ItemStack stack = rootChestInventory.getStack(i);
                    if (!stack.isEmpty()) {
                        ItemStack singleItem = stack.split(1); // Remove one item from the current stack
                        ItemStack remaining = transferItem(inventoryUtils, singleItem);

                        if (remaining.isEmpty()) {
                            rootChestInventory.setStack(i, stack);
                            return true;
                        } else {
                            stack.increment(1); // Revert the split if the item was not transferred
                            rootChestInventory.setStack(i, stack);
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * Checks if the inventory contains the specified item.
     *
     * @param inventory
     * @param item
     * @return
     */
    private static boolean containsItem(Inventory inventory, ItemStack item) {
        for (int i = 0; i < inventory.size(); i++) {
            if (ItemStack.canCombine(inventory.getStack(i), item)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Transfers an item from one inventory to another.
     *
     * @param to
     * @param item
     * @return
     */
    private static ItemStack transferItem(Inventory to, ItemStack item) {
        int size = to.size();
        for (int i = 0; i < size; i++) {
            ItemStack stackInSlot = to.getStack(i);
            if (stackInSlot.isEmpty()) {
                // If the slot is empty, transfer the item to this slot
                to.setStack(i, item);
                return ItemStack.EMPTY;
            } else if (ItemStack.canCombine(stackInSlot, item)) {
                // If the item is the same
                int transferAmount = Math.min(item.getCount(), stackInSlot.getMaxCount() - stackInSlot.getCount());
                stackInSlot.increment(transferAmount);
                item.decrement(transferAmount);
                if (item.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
        }

        // If the item could not be transferred to any slot, return it back
        return item;
    }

    /**
     * Finds all connected pipes starting from the given position.
     *
     * @param world
     * @param startPos
     * @return
     */
    private static Set<BlockPos> findConnectedPipes(World world, BlockPos startPos) {
        Set<BlockPos> visited = new HashSet<>();
        BlockPos belowPos = startPos.down();

        if (!(world.getBlockState(belowPos).getBlock() instanceof PipeBlock)) {
            return visited;
        }

        Queue<BlockPos> queue = new LinkedList<>();
        queue.add(belowPos);

        while (!queue.isEmpty()) {
            BlockPos currentPos = queue.poll();
            if (!visited.contains(currentPos)) {
                visited.add(currentPos);

                for (Direction direction : Direction.values()) {
                    BlockPos neighborPos = currentPos.offset(direction);
                    if (world.getBlockState(neighborPos).getBlock() instanceof PipeBlock) {
                        queue.add(neighborPos);
                    }
                }
            }
        }

        return visited;
    }

    /**
     * Finds all connected filters starting from the given pipe positions.
     *
     * @param world
     * @param pipePositions
     * @return
     */
    private static Set<BlockPos> findConnectedFilters(World world, Set<BlockPos> pipePositions) {
        Set<BlockPos> filterPositions = new HashSet<>();

        for (BlockPos pipePos : pipePositions) {
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = pipePos.offset(direction);
                BlockState neighborState = world.getBlockState(neighborPos);
                if (neighborState.getBlock() instanceof FilterBlock) {
                    Direction filterFacing = neighborState.get(FilterBlock.FACING);
                    if (direction == filterFacing) {
                        filterPositions.add(neighborPos);
                    }
                }
            }
        }

        return filterPositions;
    }
}