package cz.lukesmith.automaticsorter.block.custom;

import com.mojang.serialization.MapCodec;
import cz.lukesmith.automaticsorter.block.entity.ModBlockEntities;
import cz.lukesmith.automaticsorter.block.entity.SorterControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SorterControllerBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 16, 16);

    // new 1.4.0
    /*
    private static final VoxelShape SHAPE = createCuboidShape(0, 0, 0, 16, 16, 16);
    public static final EnumProperty<Direction> FACING;

    static {
        FACING = FacingBlock.FACING;
    }*/


    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    public SorterControllerBlock(Properties settings) {
        super(settings);

        // new 1.4.0
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SorterControllerBlockEntity(pPos, pState);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        if(pLevel.isClientSide()) {
            return null;
        }

        return createTickerHelper(pBlockEntityType, ModBlockEntities.SORTER_CONTROLLER_BLOCK_ENTITY.get(),
                (level, blockPos, blockState, sorterControllerBlockEntity) -> sorterControllerBlockEntity.tick(level, blockPos, blockState));
    }

    // new 1.4.0
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = ((SorterControllerBlockEntity) world.getBlockEntity(pos));

            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getSide().getOpposite());
    }
}
