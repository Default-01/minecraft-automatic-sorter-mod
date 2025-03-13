package cz.lukesmith.automaticsorter.item;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ModItemGroups {

    private static final ItemGroup ITEM_GROUP = FabricItemGroup.builder(new Identifier("tutorial", "test_group"))
            .icon(() -> new ItemStack(ModBlocks.PIPE_BLOCK)).entries(((displayContext, entries) -> {
                entries.add(ModBlocks.PIPE_BLOCK);
                entries.add(ModBlocks.SORTER_CONTROLLER_BLOCK);
                entries.add(ModBlocks.FILTER_BLOCK);
            }))
            .build();

    public static void registerItemGroups() {
        AutomaticSorter.LOGGER.info("Registering item groups for " + AutomaticSorter.MOD_ID);
    }
}