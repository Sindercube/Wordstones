package org.sindercube.wordstones.content.block;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
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
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;
import org.sindercube.wordstones.content.packet.WordstoneEditS2CPacket;
import org.sindercube.wordstones.content.packet.WordstoneTeleportS2CPacket;

public abstract class AbstractWordstoneBlock extends BlockWithEntity {

	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = Properties.DOUBLE_BLOCK_HALF;

	public static final VoxelShape
		BOTTOM_MODEL = Block.createCuboidShape(0, 0, 0, 16, 8, 16),
		BASE_MODEL = Block.createCuboidShape(2, 8, 2, 14, 24, 14),
		TOP_MODEL = Block.createCuboidShape(0, 24, 0, 16, 32, 16),
		MODEL = VoxelShapes.union(BOTTOM_MODEL, BASE_MODEL, TOP_MODEL).simplify();

	public AbstractWordstoneBlock(Settings settings) {
		super(settings);
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(WATERLOGGED, false)
			.with(FACING, Direction.NORTH)
			.with(HALF, DoubleBlockHalf.LOWER)
		);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED, FACING, HALF);
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
	protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
		super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		world.setBlockState(pos.up(), this.getDefaultState()
			.with(WATERLOGGED, world.getFluidState(pos.up()).getFluid() == Fluids.WATER)
			.with(FACING, state.get(FACING))
			.with(HALF, DoubleBlockHalf.UPPER)
		);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		BlockPos pos = context.getBlockPos();
		World world = context.getWorld();
		if (pos.getY() >= world.getTopY() - 1) return null;
		if (!world.getBlockState(pos.up()).canReplace(context)) return null;

		return this.getDefaultState()
			.with(WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).getFluid() == Fluids.WATER)
			.with(FACING, context.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		if (state.get(HALF) != DoubleBlockHalf.UPPER) return super.canPlaceAt(state, world, pos);

		BlockState belowState = world.getBlockState(pos.down());
		return belowState.isOf(this) && belowState.get(HALF) == DoubleBlockHalf.LOWER;
	}

	@Override
	public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		System.out.println("TEST");
		BlockPos entityPos = state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
		WordstoneEntity entity = (WordstoneEntity) world.getBlockEntity(entityPos);
		if (entity != null) entity.onBroken();

		this.breakOtherHalf(world, pos, state, player);
		return super.onBreak(world, pos, state, player);
	}

	public void breakOtherHalf(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		DoubleBlockHalf half = state.get(HALF);
		Direction otherDirection = half == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
		BlockPos otherPos = pos.offset(otherDirection);
		if (!world.getBlockState(otherPos).isOf(this)) return;

		world.breakBlock(pos.offset(otherDirection), true, player);
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
		if (state.get(HALF) == DoubleBlockHalf.UPPER) result = result.offset(0, -1, 0);
		return result;
	}

}
