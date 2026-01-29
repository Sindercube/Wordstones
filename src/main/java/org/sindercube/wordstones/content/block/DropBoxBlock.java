package org.sindercube.wordstones.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.sindercube.wordstones.content.block.entity.DropBoxEntity;
import org.sindercube.wordstones.registry.WordstonesSoundEvents;

public class DropBoxBlock extends BlockWithEntity {

	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	public static final VoxelShape X_SHAPE = Block.createCuboidShape(2, 0, 0, 14, 16, 16);
	public static final VoxelShape Z_SHAPE = Block.createCuboidShape(0, 0, 2, 16, 16, 14);

	@Override
    public MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(DropBoxBlock::new);
    }

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new DropBoxEntity(pos, state);
	}

    public DropBoxBlock(Settings settings) {
        super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(WATERLOGGED, false)
			.with(FACING, Direction.NORTH)
		);
    }

	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED, FACING);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof DropBoxEntity dropBox)) return ActionResult.PASS;

		ActionResult result;
		if (player.isSneaking() && player.getMainHandStack().isEmpty()) {
			result = dropBox.depositItems(player);
			if (result.isAccepted()) {
				world.playSound(player, pos.getX(), pos.getY(), pos.getZ(), WordstonesSoundEvents.DROP_BOX_DEPOSIT, SoundCategory.PLAYERS, 1, 1);
				world.emitGameEvent(player,  GameEvent.BLOCK_ACTIVATE, pos);
			}
		} else {
			result = dropBox.retrieveItems(player);
			if (result.isAccepted()) {
				world.playSound(player, pos.getX(), pos.getY(), pos.getZ(), WordstonesSoundEvents.DROP_BOX_RETRIEVE, SoundCategory.PLAYERS, 1, 1);
				world.emitGameEvent(player, GameEvent.BLOCK_DEACTIVATE, pos);
			}
		}

		return result;
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return this.getDefaultState()
			.with(WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).getFluid() == Fluids.WATER)
			.with(FACING, context.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

}
