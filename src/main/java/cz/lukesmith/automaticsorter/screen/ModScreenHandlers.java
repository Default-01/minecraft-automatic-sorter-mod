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
            MENUS.register("filter_screen",
                    () -> IForgeMenuType.create((windowId, inv, data) -> {
                        // Očekává se, že data nejsou null
                        if (data == null) throw new IllegalStateException("Missing extra data for FilterScreenHandler!");
                        return new FilterScreenHandler(windowId, inv, data);
                    }));



    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
