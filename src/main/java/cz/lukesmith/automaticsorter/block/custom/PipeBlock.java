package cz.lukesmith.automaticsorter.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PipeBlock extends Block {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty UP = BooleanProperty.create("up");
    public static final BooleanProperty DOWN = BooleanProperty.create("down");

    public PipeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(NORTH, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(EAST, false)
                .setValue(UP, false)
                .setValue(DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(NORTH, SOUTH, WEST, EAST, UP, DOWN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        VoxelShape shape = Shapes.box(6 / 16.0, 6 / 16.0, 6 / 16.0, 10 / 16.0, 10 / 16.0, 10 / 16.0);

        int[][] directions = {
                {6, 6, 0, 10, 10, 6},
                {6, 6, 10, 10, 10, 16},
                {10, 6, 6, 16, 10, 10},
                {0, 6, 6, 6, 10, 10},
                {6, 10, 6, 10, 16, 10},
                {6, 0, 6, 10, 6, 10}
        };

        boolean[] directionsActive = {
                state.getValue(NORTH),
                state.getValue(SOUTH),
                state.getValue(EAST),
                state.getValue(WEST),
                state.getValue(UP),
                state.getValue(DOWN)
        };

        for (int i = 0; i < directions.length; i++) {
            if (directionsActive[i]) {
                shape = Shapes.or(shape, Shapes.box(
                        directions[i][0] / 16.0, directions[i][1] / 16.0, directions[i][2] / 16.0,
                        directions[i][3] / 16.0, directions[i][4] / 16.0, directions[i][5] / 16.0
                ));
            }
        }

        return shape;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        boolean isConnected = isConnectedToNeighbor(pNeighborState, pDirection);
        return pState.setValue(getPropertyForDirection(pDirection), isConnected);
    }

    private boolean isConnectedToNeighbor(BlockState neighborState, Direction direction) {
        if (direction == Direction.UP && neighborState.getBlock() instanceof SorterControllerBlock) {
            return true;
        } else if (neighborState.getBlock() instanceof FilterBlock) {
            Direction filterFacing = neighborState.getValue(FilterBlock.FACING);
            return direction == filterFacing;
        } else return neighborState.getBlock() instanceof PipeBlock;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, world, pos, oldState, isMoving);

        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = world.getBlockState(neighborPos);
            state = state.setValue(getPropertyForDirection(direction), isConnectedToNeighbor(neighborState, direction));
        }

        world.setBlock(pos, state, 3);
    }

    private BooleanProperty getPropertyForDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> NORTH;
            case EAST -> EAST;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }
}