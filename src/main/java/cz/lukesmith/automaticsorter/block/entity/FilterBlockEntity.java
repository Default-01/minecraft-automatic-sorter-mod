package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.AutomaticSorter;
import cz.lukesmith.automaticsorter.network.SyncFilterTextPayload;
import cz.lukesmith.automaticsorter.screen.FilterScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class FilterBlockEntity extends BlockEntity implements ImplementedInventory, ExtendedScreenHandlerFactory<BlockPos> {

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(24, ItemStack.EMPTY);

    private int filterType = FilterTypeEnum.IN_INVENTORY.getValue();
    private String textFilter = "";

    public FilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FILTER_BLOCK_ENTITY, pos, state);
    }

    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        Inventories.writeNbt(nbt, inventory, registryLookup);
        nbt.putInt("FilterType", filterType);
        nbt.putString("TextFilter", textFilter);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        Inventories.readNbt(nbt, inventory, registryLookup);
        filterType = nbt.getInt("FilterType");
        textFilter = nbt.getString("TextFilter");
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbt = super.toInitialChunkDataNbt(registryLookup);
        nbt.putInt("FilterType", filterType);
        nbt.putString("TextFilter", textFilter);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return this.pos;
    }

    public static FilterBlockEntity create(BlockPos pos, BlockState state) {
        return new FilterBlockEntity(pos, state);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(AutomaticSorter.MOD_ID + ".filter_block_entity");
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        // Send the text filter value to the client when the screen is opened
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new SyncFilterTextPayload(this.pos, this.textFilter));
        }
        
        return new FilterScreenHandler(syncId, playerInventory, this, new PropertyDelegate() {
            @Override
            public int get(int index) {
                if (index == 0) {
                    return FilterBlockEntity.this.filterType;
                }
                return 0;
            }

            @Override
            public void set(int index, int value) {
                if (index == 0) {
                    FilterBlockEntity.this.filterType = value;
                }
            }

            @Override
            public int size() {
                return 1;
            }
        });
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world.isClient) {
            // Tick metoda se neprovádí na klientské straně
        }
    }

    public int getFilterType() {
        return filterType;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (world != null && !world.isClient()) {
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
        }
    }

    public void setFilterType(int filterType) {
        // If switching to TEXT_FILTER mode, clear the inventory
        if (filterType == FilterTypeEnum.TEXT_FILTER.getValue() && this.filterType != filterType) {
            for (int i = 0; i < inventory.size(); i++) {
                inventory.set(i, ItemStack.EMPTY);
            }
        }
        this.filterType = filterType;
        this.markDirty();
    }

    public boolean isItemInInventory(ItemStack singleItem) {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty() && ItemStack.areItemsEqual(stack, singleItem)) {
                return true;
            }
        }
        return false;
    }

    public enum FilterTypeEnum {
        WHITELIST(0),
        IN_INVENTORY(1),
        REJECTS(2),
        TEXT_FILTER(3);

        private final int value;


        FilterTypeEnum(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static FilterTypeEnum fromValue(int value) {
            for (FilterTypeEnum type : values()) {
                if (type.getValue() == value) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unknown enum value: " + value);
        }

        public static int nextValue(int number) {
            return switch (FilterTypeEnum.fromValue(number)) {
                case WHITELIST -> FilterTypeEnum.IN_INVENTORY.getValue();
                case IN_INVENTORY -> FilterTypeEnum.REJECTS.getValue();
                case REJECTS -> FilterTypeEnum.TEXT_FILTER.getValue();
                case TEXT_FILTER -> FilterTypeEnum.WHITELIST.getValue();
            };
        }

        public static String getName(FilterTypeEnum type) {
            return switch (type) {
                case WHITELIST -> "Whitelist";
                case IN_INVENTORY -> "In Inventory";
                case REJECTS -> "Rejects";
                case TEXT_FILTER -> "Text Filter";
            };
        }

        public static String getName(int value) {
            return getName(fromValue(value));
        }
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    public String getTextFilter() {
        return textFilter;
    }

    public void setTextFilter(String textFilter) {
        this.textFilter = textFilter;
        this.markDirty();
    }

    public boolean matchesTextFilter(ItemStack itemStack) {
        if (textFilter == null || textFilter.trim().isEmpty()) {
            return false;
        }

        String itemName = itemStack.getItem().toString().toLowerCase();
        String translationKey = itemStack.getTranslationKey().toLowerCase();
        String filterPattern = textFilter.toLowerCase().trim();

        // Helper to match a single pattern (wildcards)
        java.util.function.Predicate<String> matchPattern = pattern -> {
            pattern = pattern.trim();
            if (pattern.isEmpty()) return false;
            String regex = pattern.replace(".", "\\.").replace("*", ".*").replace("?", ".");
            return itemName.matches(regex) || translationKey.matches(regex);
        };

        // Recursive parser for AND/OR/NOT/parentheses
        class Expr {
            boolean eval(String expr) {
                expr = expr.trim();
                if (expr.isEmpty()) return false;
                // NOT (!)
                if (expr.startsWith("!")) {
                    String sub = expr.substring(1);
                    return !eval(sub);
                }
                // Parentheses (only if not a single pattern)
                if (expr.startsWith("(") && expr.endsWith(")") && expr.length() > 2) {
                    return eval(expr.substring(1, expr.length() - 1));
                }
                // OR
                int orIdx = findTopLevel(expr, '|');
                if (orIdx >= 0) {
                    String left = expr.substring(0, orIdx);
                    String right = expr.substring(orIdx + 1);
                    return eval(left) || eval(right);
                }
                // AND
                int andIdx = findTopLevel(expr, '&');
                if (andIdx >= 0) {
                    String left = expr.substring(0, andIdx);
                    String right = expr.substring(andIdx + 1);
                    return eval(left) && eval(right);
                }
                // Single pattern (no operators)
                return matchPattern.test(expr);
            }

            // Find top-level operator (not inside parentheses)
            int findTopLevel(String expr, char op) {
                int depth = 0;
                for (int i = 0; i < expr.length(); i++) {
                    char c = expr.charAt(i);
                    if (c == '(') depth++;
                    else if (c == ')') depth--;
                    else if (c == op && depth == 0) return i;
                }
                return -1;
            }
        }

        Expr parser = new Expr();
        return parser.eval(filterPattern);
    }
}
