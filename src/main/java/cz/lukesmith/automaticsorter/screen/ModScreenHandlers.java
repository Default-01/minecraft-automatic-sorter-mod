package cz.lukesmith.automaticsorter.screen;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModScreenHandlers {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, AutomaticSorter.MOD_ID);

    public static final RegistryObject<MenuType<FilterScreenHandler>> FILTER_SCREEN_HANDLER =
            MENUS.register("filter_screen", () -> IForgeMenuType.create(FilterScreenHandler::new));


    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }


    /*public static final ScreenHandlerType<FilterScreenHandler> FILTER_SCREEN_HANDLER =
            Registry.register(Registries.SCREEN_HANDLER, Identifier.of(AutomaticSorter.MOD_ID, "gem_polishing"),
                    new ExtendedScreenHandlerType<>(FilterScreenHandler::new, BlockPos.PACKET_CODEC));

    public static void registerScreenHandlers() {
        AutomaticSorter.LOGGER.info("Registering Screen Handlers for " + AutomaticSorter.MOD_ID);
    }*/
}
