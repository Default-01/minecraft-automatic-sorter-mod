package cz.lukesmith.automaticsorter.block;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.custom.FilterBlock;
import cz.lukesmith.automaticsorter.block.custom.PipeBlock;
import cz.lukesmith.automaticsorter.block.custom.SorterControllerBlock;
import cz.lukesmith.automaticsorter.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AutomaticSorter.MOD_ID);

    public static final RegistryObject<Block> PIPE_BLOCK = registerBlock("pipe",
            () -> new PipeBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(AutomaticSorter.MOD_ID, "pipe")))
                    .strength(1.0f, 2.0f).noOcclusion()));

    public static final RegistryObject<Block> SORTER_CONTROLLER_BLOCK = registerBlock("sorter_controller",
            () -> new SorterControllerBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(AutomaticSorter.MOD_ID, "sorter_controller")))
                    .strength(3.0f, 6.0f).noOcclusion()));

    public static final RegistryObject<Block> FILTER_BLOCK = registerBlock("filter",
            () -> new FilterBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(AutomaticSorter.MOD_ID, "filter")))
                    .strength(3.0f, 6.0f).noOcclusion()));

    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    /*
    public static final Block PIPE_BLOCK = registerBlock("pipe",
            new PipeBlock(Block.Settings.copy(Blocks.COPPER_BLOCK)
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(AutomaticSorter.MOD_ID, "pipe")))
                    .requiresTool().strength(1.0f, 2.0f).nonOpaque()
    ));
    public static final Block SORTER_CONTROLLER_BLOCK = registerBlock("sorter_controller",
            new SorterControllerBlock(Block.Settings.copy(Blocks.COPPER_BLOCK)
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(AutomaticSorter.MOD_ID, "sorter_controller")))
                    .requiresTool().strength(3.0f, 6.0f).nonOpaque()
    ));
    public static final Block FILTER_BLOCK = registerBlock("filter",
            new FilterBlock(Block.Settings.copy(Blocks.COPPER_BLOCK)
                    .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(AutomaticSorter.MOD_ID, "filter")))
                    .requiresTool().strength(3.0f, 6.0f).nonOpaque()
    ));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, Identifier.of(AutomaticSorter.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(AutomaticSorter.MOD_ID, name),
                new BlockItem(block, new Item.Settings()
                        .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(AutomaticSorter.MOD_ID, name))).useBlockPrefixedTranslationKey()));
    }

    public static void registerModBlocks() {
        AutomaticSorter.LOGGER.info("Registering ModBlocks for " + AutomaticSorter.MOD_ID);
    }*/
}
