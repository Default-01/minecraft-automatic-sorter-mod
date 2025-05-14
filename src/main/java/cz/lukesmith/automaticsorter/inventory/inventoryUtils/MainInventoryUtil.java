package cz.lukesmith.automaticsorter.inventory.inventoryUtils;

import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.IInventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.InventoryAdapter;
import cz.lukesmith.automaticsorter.inventory.inventoryAdapters.NoInventoryAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class MainInventoryUtil {

    @NotNull
    public static IInventoryAdapter getInventoryAdapter(World world, BlockPos pos) {

        IInventoryUtil expandedIU = new ExpandedInventoryUtil();
        IInventoryUtil assortedIU = new AssortedInventoryUtil();

        Block block = world.getBlockState(pos).getBlock();
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (block instanceof ChestBlock chestBlock) {
            return new InventoryAdapter(ChestBlock.getContainer(chestBlock, world.getBlockState(pos), world, pos, true));
        } else if (blockEntity instanceof Inventory inventory) {
            if (expandedIU.isRelatedStorage(block, blockEntity)) {
                return expandedIU.getInventoryAdapter(world, pos, block, blockEntity);
            } else {
                return new InventoryAdapter(inventory);
            }
        } else {
            if (assortedIU.isRelatedStorage(block, blockEntity)) {
                return assortedIU.getInventoryAdapter(world, pos, block, blockEntity);
            }
        }

        return new NoInventoryAdapter();
    }
}