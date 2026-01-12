package org.sindercube.wordstones.content.block;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;
import org.sindercube.wordstones.content.packet.WordstoneEditS2CPacket;
import org.sindercube.wordstones.content.packet.WordstoneTeleportS2CPacket;

public abstract class AbstractWordstoneBlock extends BlockWithEntity {

	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

	public static final VoxelShape
		BOTTOM_MODEL = Block.createCuboidShape(0, 0, 0, 16, 8, 16),
		BASE_MODEL = Block.createCuboidShape(2, 8, 2, 14, 24, 14),
		TOP_MODEL = Block.createCuboidShape(0, 24, 0, 16, 32, 16),
		MODEL = VoxelShapes.union(BOTTOM_MODEL, BASE_MODEL, TOP_MODEL);

	public AbstractWordstoneBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(FACING, Direction.NORTH)
			.with(HALF, DoubleBlockHalf.LOWER)
		);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF);
	}

	public void openEditScreen(PlayerEntity player, WordstoneEntity entity) {
		if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
		ServerPlayNetworking.send(serverPlayer, new WordstoneEditS2CPacket(entity.getPos()));
	}

	public void openTeleportScreen(PlayerEntity player, WordstoneEntity entity) {
		if (!(player instanceof ServerPlayerEntity serverPlayer)) return;
		ServerPlayNetworking.send(serverPlayer, new WordstoneTeleportS2CPacket(entity.getPos()));
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		DoubleBlockHalf doubleBlockHalf = state.get(HALF);
		if (direction.getAxis() != Direction.Axis.Y || doubleBlockHalf == DoubleBlockHalf.LOWER != (direction == Direction.UP) || neighborState.isOf(this) && neighborState.get(HALF) != doubleBlockHalf) {
			return doubleBlockHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
		} else {
			return Blocks.AIR.getDefaultState();
		}
	}

	@Nullable
	public BlockState getPlacementState(ItemPlacementContext context) {
		BlockPos pos = context.getBlockPos();
		World world = context.getWorld();
		if (pos.getY() >= world.getTopY() - 1) return null;
		if (!world.getBlockState(pos.up()).canReplace(context)) return null;
		return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (state.get(HALF) != DoubleBlockHalf.UPPER) return super.canPlaceAt(state, world, pos);

		BlockState belowState = world.getBlockState(pos.down());
		return belowState.isOf(this) && belowState.get(HALF) == DoubleBlockHalf.LOWER;
	}

	public static BlockState withWaterloggedState(WorldView world, BlockPos pos, BlockState state) {
		return state.contains(Properties.WATERLOGGED) ? state.with(Properties.WATERLOGGED, world.isWater(pos)) : state;
	}

	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		if (!world.isClient && (player.isCreative() || !player.canHarvest(state))) {
			TallPlantBlock.onBreakInCreative(world, pos, state, player);
		}

		return super.onBreak(world, pos, state, player);
	}

	@Override
	protected BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		VoxelShape result = MODEL;
		if (state.get(HALF) == DoubleBlockHalf.UPPER)
			result = result.offset(0, -1, 0);
		return result;
	}

}
