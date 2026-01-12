package org.sindercube.wordstones.content.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.sindercube.wordstones.content.block.entity.SteleEntity;
import org.sindercube.wordstones.content.packet.SteleEditS2CPacket;

public class SteleBlock extends AbstractSignBlock {

	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final BooleanProperty HANGING = Properties.HANGING;

	public static final VoxelShape X_SHAPE = Block.createCuboidShape(6, 0, 0, 10, 12, 16);
	public static final VoxelShape Z_SHAPE = Block.createCuboidShape(0, 0, 6, 16, 12, 10);

	public static final MapCodec<SteleBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		WoodType.CODEC.fieldOf("wood_type").forGetter(block -> block.getWoodType()),
		createSettingsCodec()
	).apply(instance, SteleBlock::new));

	@Override
	public MapCodec<? extends AbstractSignBlock> getCodec() {
		return CODEC;
	}

	public SteleBlock(WoodType type, Settings settings) {
		super(type, settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(HANGING, false).with(WATERLOGGED, false));
	}

	public static Direction attachedDirection(BlockState state) {
		return state.get(HANGING) ? Direction.DOWN : Direction.UP;
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, HANGING, WATERLOGGED);
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new SteleEntity(pos, state);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
		super.onPlaced(world, pos, state, placer, itemStack);
		if (placer instanceof ServerPlayerEntity player) {
			SteleEntity entity = (SteleEntity) world.getBlockEntity(pos);
			this.openEditScreen(player, entity, true);
		}
	}

	@Override
	public void openEditScreen(PlayerEntity player, SignBlockEntity entity, boolean front) {
		if (!(player instanceof ServerPlayerEntity serverPlayer)) return;

		entity.setEditor(player.getUuid());
		ServerPlayNetworking.send(serverPlayer, new SteleEditS2CPacket(entity.getPos(), front));
	}

	@Override
	protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		Direction direction = attachedDirection(state).getOpposite();
		return world.getBlockState(pos.offset(direction)).isSolid();
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		if (context.getSide().getAxis().isHorizontal()) return null;
		return this.getDefaultState()
			.with(FACING, context.getHorizontalPlayerFacing().getOpposite())
			.with(HANGING, context.getSide() == Direction.DOWN)
			.with(WATERLOGGED, context.getWorld().getFluidState(context.getBlockPos()).getFluid() == Fluids.WATER);
	}

	@Override
	protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return attachedDirection(state).getOpposite() == direction && !state.canPlaceAt(world, pos) ? Blocks.AIR.getDefaultState() : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
	}

	@Override
	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	public float getRotationDegrees(BlockState state) {
		return state.get(FACING).asRotation();
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		VoxelShape shape = state.get(FACING).getAxis() == Direction.Axis.Z ? Z_SHAPE : X_SHAPE;
		if (state.get(HANGING)) return shape.offset(0, (float) 4 / 16, 0);
		return shape;
	}

}
