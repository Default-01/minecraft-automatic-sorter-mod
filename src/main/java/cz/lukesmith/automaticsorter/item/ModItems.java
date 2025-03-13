package cz.lukesmith.automaticsorter.item;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registry.ITEM, new Identifier(AutomaticSorter.MOD_ID, name), item);
    }

    public static void registerItems() {
        AutomaticSorter.LOGGER.info("Registering Mod items for " + AutomaticSorter.MOD_ID);
    }
}