package org.sindercube.wordstones.content.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;
import org.sindercube.wordstones.content.block.entity.SteleEntity;
import org.sindercube.wordstones.content.packet.SteleEditS2CPacket;

public class SteleBlock extends AbstractSignBlock {

	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
	public static final EnumProperty<SlabType> TYPE = Properties.SLAB_TYPE;
	public static final BooleanProperty ATTACHED = Properties.ATTACHED;

	public static final VoxelShape X_SHAPE = Block.createCuboidShape(6, 0, 0, 10, 8, 16);
	public static final VoxelShape Z_SHAPE = Block.createCuboidShape(0, 0, 6, 16, 8, 10);

	public static final float VERTICAL_OFFSET = 0.5F;
	public static final float ATTACHED_OFFSET = 0.375F;

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
		this.setDefaultState(this.stateManager.getDefaultState()
			.with(FACING, Direction.NORTH)
			.with(TYPE, SlabType.BOTTOM)
			.with(ATTACHED, false)
			.with(WATERLOGGED, false)
		);
	}

	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING, TYPE, ATTACHED, WATERLOGGED);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
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
		if (player instanceof ServerPlayerEntity serverPlayer) {
			entity.setEditor(player.getUuid());
			ServerPlayNetworking.send(serverPlayer, new SteleEditS2CPacket(entity.getPos(), front));
		}
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		BlockPos pos = context.getBlockPos();
		BlockState existingState = context.getWorld().getBlockState(pos);
		if (existingState.isOf(this)) {
			return existingState.with(TYPE, SlabType.DOUBLE);
		}

		Direction direction;
		boolean attached;
		if (context.getSide().getAxis().isHorizontal()) {
			direction = context.getSide();
			attached = true;
		} else {
			direction = context.getHorizontalPlayerFacing().getOpposite();
			attached = false;
		}

		return this.getDefaultState()
			.with(TYPE, getPlacementType(context))
			.with(FACING, direction)
			.with(ATTACHED, attached)
			.with(WATERLOGGED, context.getWorld().getFluidState(pos).getFluid() == Fluids.WATER);
	}

	public static SlabType getPlacementType(ItemPlacementContext context) {
		return switch (context.getSide().getOpposite()) {
			case UP -> SlabType.TOP;
			case DOWN -> SlabType.BOTTOM;
			default -> context.getHitPos().y - context.getBlockPos().getY() > 0.5F ? SlabType.TOP : SlabType.BOTTOM;
		};
	}

	@Override
	public boolean canReplace(BlockState state, ItemPlacementContext context) {
		ItemStack stack = context.getStack();
		SlabType type = state.get(TYPE);
		if (type == SlabType.DOUBLE) return false;
		if (!stack.isOf(this.asItem())) return false;

		if (!context.canReplaceExisting()) return true;

		boolean hitTopHalf = context.getHitPos().y - context.getBlockPos().getY() > 0.5F;
		Direction direction = context.getSide();
		if (type == SlabType.BOTTOM) {
			return direction == Direction.UP || hitTopHalf && direction.getAxis().isHorizontal();
		}
		return direction == Direction.DOWN || !hitTopHalf && direction.getAxis().isHorizontal();
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
		if (state.get(WATERLOGGED)) {
			world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
		}

		return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
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
		VoxelShape shape = state.get(FACING).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
		shape = switch (state.get(TYPE)) {
			case BOTTOM -> shape;
			case TOP -> shape.offset(0, VERTICAL_OFFSET, 0);
			case DOUBLE -> VoxelShapes.union(shape, shape.offset(0, VERTICAL_OFFSET, 0)).simplify();
		};
		if (state.get(ATTACHED)) shape = switch (state.get(FACING)) {
			case NORTH -> shape.offset(0, 0, ATTACHED_OFFSET);
			case EAST -> shape.offset(-ATTACHED_OFFSET, 0, 0);
			case SOUTH -> shape.offset(0, 0, -ATTACHED_OFFSET);
			case WEST -> shape.offset(ATTACHED_OFFSET, 0, 0);
			default -> shape;
		};
		return shape;
	}

}
