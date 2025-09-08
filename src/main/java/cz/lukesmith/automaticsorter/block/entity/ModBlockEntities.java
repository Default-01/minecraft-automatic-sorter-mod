package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import cz.lukesmith.automaticsorter.block.ModBlocks;

import java.util.Set;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AutomaticSorter.MOD_ID);

    public static final RegistryObject<BlockEntityType<SorterControllerBlockEntity>> SORTER_CONTROLLER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("sorter_controller_be",
                    () -> BlockEntityType.Builder.of(SorterControllerBlockEntity::new, ModBlocks.SORTER_CONTROLLER_BLOCK.get()).build(null));

    public static final RegistryObject<BlockEntityType<FilterBlockEntity>> FILTER_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("filter_be",
                    () -> BlockEntityType.Builder.of(FilterBlockEntity::new, ModBlocks.FILTER_BLOCK.get()).build(null));


    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}