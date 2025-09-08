package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.block.custom.FilterBlock;
import cz.lukesmith.automaticsorter.block.custom.PipeBlock;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.NoInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryUtils.MainInventoryUtil;
import net.minecraft.core.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class SorterControllerBlockEntity extends BlockEntity {

    private int ticker = 0;
    private static final int MAX_TICKER = 5;

    public SorterControllerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SORTER_CONTROLLER_BLOCK_ENTITY.get(), pos, state);
    }

    public static SorterControllerBlockEntity create(BlockPos pos, BlockState state) {
        return new SorterControllerBlockEntity(pos, state);
    }

    private static boolean tryWhitelistMode(IInventoryAdapter rootInventoryAdapter, IInventoryAdapter chestInventoryAdapter, IInventoryAdapter filterInventoryAdapter) {
        if (rootInventoryAdapter.isEmpty()) {
            return false;
        }

        ArrayList<ItemStack> stacks = rootInventoryAdapter.getAllStacks();
        int stackSize = stacks.size();
        for (int i = 0; i < stackSize; i++) {
            ItemStack rootInventoryItemStack = stacks.get(i);
            if (rootInventoryItemStack.isEmpty()) {
                continue;
            }

            ItemStack containItemStack = filterInventoryAdapter.containsItem(rootInventoryItemStack);
            if (!containItemStack.isEmpty()) {
                if (chestInventoryAdapter.addItem(rootInventoryItemStack)) {
                    rootInventoryAdapter.removeItem(i, 1);
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean tryInInventoryMode(IInventoryAdapter rootInventoryAdapter, IInventoryAdapter chestInventoryAdapter) {
        if (rootInventoryAdapter.isEmpty()) {
            return false;
        }

        ArrayList<ItemStack> stacks = rootInventoryAdapter.getAllStacks();
        int stackSize = stacks.size();
        for (int i = 0; i < stackSize; i++) {
            ItemStack rootInventoryItemStack = stacks.get(i);
            if (rootInventoryItemStack.isEmpty()) {
                continue;
            }

            ItemStack containItemStack = chestInventoryAdapter.containsItem(rootInventoryItemStack);
            if (!containItemStack.isEmpty()) {
                if (chestInventoryAdapter.addItem(rootInventoryItemStack)) {
                    rootInventoryAdapter.removeItem(i, 1);
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean tryRejectsMode(IInventoryAdapter rootInventoryAdapter, IInventoryAdapter chestInventoryAdapter) {
        if (rootInventoryAdapter.isEmpty()) {
            return false;
        }

        ArrayList<ItemStack> stacks = rootInventoryAdapter.getAllStacks();
        int stackSize = stacks.size();
        for (int i = 0; i < stackSize; i++) {
            ItemStack rootInventoryItemStack = stacks.get(i);
            if (rootInventoryItemStack.isEmpty()) {
                continue;
            }

            if (chestInventoryAdapter.addItem(rootInventoryItemStack)) {
                rootInventoryAdapter.removeItem(i, 1);
                return true;
            }
        }

        return false;
    }

    private static boolean tryToTransferItem(Level world, FilterBlockEntity filterBlockEntity, BlockPos filterPos, IInventoryAdapter rootInventoryAdapter) {
        Direction filterDirection = world.getBlockState(filterPos).getValue(FilterBlock.FACING);
        BlockPos chestPos = filterPos.relative(filterDirection);
        IInventoryAdapter chestInventoryAdapter = MainInventoryUtil.getInventoryAdapter(world, chestPos);
        if (chestInventoryAdapter instanceof NoInventoryAdapter) {
            return false;
        }

        int filterType = filterBlockEntity.getFilterType();
        return switch (FilterBlockEntity.FilterTypeEnum.fromValue(filterType)) {
            case WHITELIST -> {
                IInventoryAdapter filterInventoryAdapter = MainInventoryUtil.getInventoryAdapter(world, filterPos);
                yield tryWhitelistMode(rootInventoryAdapter, chestInventoryAdapter, filterInventoryAdapter);
            }
            case IN_INVENTORY -> tryInInventoryMode(rootInventoryAdapter, chestInventoryAdapter);
            case REJECTS -> tryRejectsMode(rootInventoryAdapter, chestInventoryAdapter);
            default -> false;
        };

    }

    public void tick(Level world, BlockPos pos, BlockState state) {
        if (world.isClientSide) {
            return;
        }

        if (ticker > 0) {
            ticker--;
            return;
        }

        Set<BlockPos> visited = new HashSet<>();
        BlockPos belowPos = pos.below();

        if ((world.getBlockState(belowPos).getBlock() instanceof PipeBlock)) {
            Queue<BlockPos> queue = new LinkedList<>();
            queue.add(belowPos);

            IInventoryAdapter rootInventoryAdapter = MainInventoryUtil.getInventoryAdapter(world, pos.above());
            boolean noInventoryRootChest = rootInventoryAdapter instanceof NoInventoryAdapter;
            boolean itemTransfered = false;
            ArrayList<FilterBlockEntity> rejectedFilters = new ArrayList<>();

            while (!queue.isEmpty() && !itemTransfered && !noInventoryRootChest) {
                BlockPos currentPos = queue.poll();
                if (visited.contains(currentPos)) {
                    continue;
                }

                visited.add(currentPos);
                for (Direction direction : Direction.values()) {
                    BlockPos neighborPos = currentPos.relative(direction);
                    Block block = world.getBlockState(neighborPos).getBlock();
                    if (block instanceof PipeBlock) {
                        queue.add(neighborPos);
                    } else if (block instanceof FilterBlock) {
                        BlockEntity filterEntity = world.getBlockEntity(neighborPos);
                        if (filterEntity instanceof FilterBlockEntity filterBlockEntity) {
                            Direction filterFacing = world.getBlockState(neighborPos).getValue(FilterBlock.FACING);
                            if (direction != filterFacing) {
                                continue;
                            }

                            if (filterBlockEntity.getFilterType() == FilterBlockEntity.FilterTypeEnum.REJECTS.getValue()) {
                                rejectedFilters.add(filterBlockEntity);
                                continue;
                            }

                            if (tryToTransferItem(world, filterBlockEntity, neighborPos, rootInventoryAdapter)) {
                                itemTransfered = true;
                                break;
                            }
                        }
                    }
                }
            }

            if (!itemTransfered) {
                for (FilterBlockEntity filterBlockEntity : rejectedFilters) {
                    if (tryToTransferItem(world, filterBlockEntity, filterBlockEntity.getBlockPos(), rootInventoryAdapter)) {
                        break;
                    }
                }
            }
        }

        ticker = MAX_TICKER;
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookup) {
        super.onDataPacket(connection, pkt, lookup);
    }
}