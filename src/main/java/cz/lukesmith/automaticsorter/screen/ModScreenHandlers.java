package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModScreenHandlers {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, AutomaticSorter.MOD_ID);

    public static final RegistryObject<MenuType<FilterScreenHandler>> FILTER_SCREEN_HANDLER =
            MENUS.register("filter_screen",
                    () -> IForgeMenuType.create(FilterScreenHandler::new));

    public static final RegistryObject<MenuType<SorterControllerScreenHandler>> SORTER_CONTROLLER_SCREEN_HANDLER =
            MENUS.register("sorter_controller_screen",
                    () -> IForgeMenuType.create(SorterControllerScreenHandler::new));


    public static void register(BusGroup eventBus) {
        MENUS.register(eventBus);
    }
}
