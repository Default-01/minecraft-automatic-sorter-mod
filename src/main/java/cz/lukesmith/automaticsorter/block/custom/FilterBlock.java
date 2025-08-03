package cz.lukesmith.automaticsorter.block.custom;

import com.mojang.serialization.MapCodec;
import cz.lukesmith.automaticsorter.block.entity.FilterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;

import javax.swing.text.html.BlockView;
import java.util.List;

public class FilterBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class);
    public static final MapCodec<FilterBlock> CODEC = simpleCodec(FilterBlock::new);

    public FilterBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return super.getShape(pState, pLevel, pPos, pContext);
    }


    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        return super.getDrops(pState, pParams);
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, CollisionContext context) {
        Vec3[][] shapes = getShapesForFacing(state.getValue(FACING));

        VoxelShape shape = Shapes.empty();
        for (Vec3[] shapePart : shapes) {
            shapePart[0] = shapePart[0].scale(1 / 16.0);
            shapePart[1] = shapePart[1].scale(1 / 16.0);
            shape = Shapes.join(shape, Shapes.box(shapePart[0].get(Direction.Axis.X), shapePart[0].get(Direction.Axis.Y), shapePart[0].get(Direction.Axis.Z), shapePart[1].get(Direction.Axis.X), shapePart[1].get(Direction.Axis.Y), shapePart[1].get(Direction.Axis.Z)), BooleanOp.AND);
        }

        return shape;
    }

    private Vec3[][] getShapesForFacing(Direction facing) {
        Vec3[][] shapes = new Vec3[][]{
                {new Vec3(6, 0, 6), new Vec3(10, 9, 10)},
                {new Vec3(3, 9, 3), new Vec3(13, 13, 13)},
                {new Vec3(2, 13, 2), new Vec3(14, 16, 14)}
        };

        switch (facing) {
            case DOWN:
                shapes[0] = new Vec3[]{new Vec3(6, 7, 6), new Vec3(10, 16, 10)};
                shapes[1] = new Vec3[]{new Vec3(3, 3, 3), new Vec3(13, 7, 13)};
                shapes[2] = new Vec3[]{new Vec3(2, 0, 2), new Vec3(14, 3, 14)};
                break;
            case NORTH:
                shapes[0] = new Vec3[]{new Vec3(6, 6, 7), new Vec3(10, 10, 16)};
                shapes[1] = new Vec3[]{new Vec3(3, 3, 3), new Vec3(13, 13, 7)};
                shapes[2] = new Vec3[]{new Vec3(2, 2, 0), new Vec3(14, 14, 3)};
                break;
            case SOUTH:
                shapes[0] = new Vec3[]{new Vec3(6, 6, 0), new Vec3(10, 10, 9)};
                shapes[1] = new Vec3[]{new Vec3(3, 3, 9), new Vec3(13, 13, 13)};
                shapes[2] = new Vec3[]{new Vec3(2, 2, 13), new Vec3(14, 14, 16)};
                break;
            case WEST:
                shapes[0] = new Vec3[]{new Vec3(7, 6, 6), new Vec3(16, 10, 10)};
                shapes[1] = new Vec3[]{new Vec3(3, 3, 3), new Vec3(7, 13, 13)};
                shapes[2] = new Vec3[]{new Vec3(0, 2, 2), new Vec3(3, 14, 14)};
                break;
            case EAST:
                shapes[0] = new Vec3[]{new Vec3(0, 6, 6), new Vec3(9, 10, 10)};
                shapes[1] = new Vec3[]{new Vec3(9, 3, 3), new Vec3(13, 13, 13)};
                shapes[2] = new Vec3[]{new Vec3(13, 2, 2), new Vec3(16, 14, 14)};
                break;
        }

        return shapes;
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new FilterBlockEntity(pPos, pState);
    }

    @Override
    public @Nullable <T extends BlockEntity> GameEventListener getListener(ServerLevel pLevel, T pBlockEntity) {
        return super.getListener(pLevel, pBlockEntity);
    }

    protected InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide) {
            MenuProvider screenHandlerFactory = ((FilterBlockEntity) world.getBlockEntity(pos));

            if (screenHandlerFactory != null) {
                player.openMenu(screenHandlerFactory);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public void onBlockStateChange(LevelReader level, BlockPos pos, BlockState oldState, BlockState newState) {
        super.onBlockStateChange(level, pos, oldState, newState);
    }

    /*@Override
    public void onRemove(BlockState oldState, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!oldState.is(newState.getBlock())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof FilterBlockEntity) {
                Containers.dropContents(world, pos, (FilterBlockEntity) be);
                world.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(oldState, world, pos, newState, isMoving);
        }
    }*/
}














