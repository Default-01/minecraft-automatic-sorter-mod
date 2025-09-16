package cz.lukesmith.automaticsorter.item;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AutomaticSorter.MOD_ID);

    // new 1.4.0
    public static final Item SORTER_AMPLIFIER = registerItem("sorter_amplifier", SorterAmplifierItem::new, new Item.Settings());

    public static void register(BusGroup eventBus) {
        ITEMS.register(eventBus);
    }
}
