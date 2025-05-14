package cz.lukesmith.automaticsorter.inventory.inventoryUtils;

import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IInventoryUtil {

    IInventoryAdapter getInventoryAdapter(World world, BlockPos pos, Block block, BlockEntity blockEntity);

    boolean isRelatedStorage(Block block, BlockEntity blockEntity);
}
