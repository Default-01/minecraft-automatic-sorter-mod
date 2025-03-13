package cz.lukesmith.automaticsorter.item;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ModItemGroups {

    private static final ItemGroup ITEM_GROUP = new ItemGroup(0, new Identifier(AutomaticSorter.MOD_ID, "automatic_sorter_group").toString()) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.PIPE_BLOCK);
        }

        @Override
        public void appendStacks(DefaultedList<ItemStack> stacks) {
            stacks.add(new ItemStack(ModBlocks.PIPE_BLOCK));
            stacks.add(new ItemStack(ModBlocks.SORTER_CONTROLLER_BLOCK));
            stacks.add(new ItemStack(ModBlocks.FILTER_BLOCK));
        }
    };

    public static void registerItemGroups() {
        AutomaticSorter.LOGGER.info("Registering item groups for " + AutomaticSorter.MOD_ID);
    }
}