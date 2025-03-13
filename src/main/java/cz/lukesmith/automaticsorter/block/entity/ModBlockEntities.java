package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModBlockEntities {
    public static BlockEntityType<FilterBlockEntity> FILTER_BLOCK_ENTITY;

    public static final BlockEntityType<SorterControllerBlockEntity> SORTER_CONTROLLER_BLOCK_ENTITY =
            Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(AutomaticSorter.MOD_ID, "sorter_controller_be"),
                    BlockEntityType.Builder.create(SorterControllerBlockEntity::create, ModBlocks.SORTER_CONTROLLER_BLOCK).build(null));

    public static void registerModBlocksEntities() {
        FILTER_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(AutomaticSorter.MOD_ID, "filter_be"), FabricBlockEntityTypeBuilder.create(FilterBlockEntity::new, ModBlocks.FILTER_BLOCK).build(null));
        AutomaticSorter.LOGGER.info("Registering ModBlocksEntities for " + AutomaticSorter.MOD_ID);
    }
}