package cz.lukesmith.automaticsorter.block.entity;

import cz.lukesmith.automaticsorter.screen.FilterScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class FilterBlockEntity extends BlockEntity implements ImplementedInventory, MenuProvider {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(24, ItemStack.EMPTY);

    private int filterType = FilterTypeEnum.IN_INVENTORY.getValue();

    public FilterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FILTER_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return null;
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        ContainerHelper.loadAllItems(pTag, inventory, pRegistries);
        filterType = pTag.getInt("FilterType").orElse(FilterTypeEnum.IN_INVENTORY.getValue());
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        ContainerHelper.saveAllItems(pTag, inventory, pRegistries);
        pTag.putInt("FilterType", filterType);
    }

    public static FilterBlockEntity create(BlockPos pos, BlockState state) {
        return new FilterBlockEntity(pos, state);
    }

    @Override
    public Component getDisplayName() {
        return null;
    }

    public int getFilterType() {
        return filterType;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null) {
            level.setBlock(getBlockPos(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void setFilterType(int filterType) {
        this.filterType = filterType;
        this.setChanged();
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return null;
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        return null;
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return null;
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {

    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return false;
    }

    @Override
    public void clearContent() {

    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new FilterScreenHandler(pContainerId, pPlayerInventory, this, new ContainerData() {
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
            public int getCount() {
                return 1;
            }
        });
    }

    public enum FilterTypeEnum {
        WHITELIST(0),
        IN_INVENTORY(1),
        REJECTS(2);

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
                case REJECTS -> FilterTypeEnum.WHITELIST.getValue();
            };
        }

        public static String getName(FilterTypeEnum type) {
            return switch (type) {
                case WHITELIST -> "Whitelist";
                case IN_INVENTORY -> "In Inventory";
                case REJECTS -> "Rejects";
            };
        }

        public static String getName(int value) {
            return getName(fromValue(value));
        }
    }

    public int getMaxCountPerStack() {
        return 1;
    }
}
