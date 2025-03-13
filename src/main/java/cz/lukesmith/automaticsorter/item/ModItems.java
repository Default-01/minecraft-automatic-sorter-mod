package cz.lukesmith.automaticsorter.item;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(AutomaticSorter.MOD_ID, name), item);
    }

    public static void registerItems() {
        AutomaticSorter.LOGGER.info("Registering Mod items for " + AutomaticSorter.MOD_ID);
    }
}
