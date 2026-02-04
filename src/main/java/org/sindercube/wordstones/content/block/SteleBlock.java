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

	public static final DirectionProperty TOP_FACING = DirectionProperty.of("top_facing", Direction.Type.HORIZONTAL);
	public static final DirectionProperty BOTTOM_FACING = DirectionProperty.of("bottom_facing", Direction.Type.HORIZONTAL);
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
			.with(TOP_FACING, Direction.NORTH)
			.with(BOTTOM_FACING, Direction.NORTH)
			.with(TYPE, SlabType.BOTTOM)
			.with(ATTACHED, false)
			.with(WATERLOGGED, false)
		);
	}

	public static Direction getRotation(BlockState state) {
		return state.get(SteleBlock.TYPE) == SlabType.TOP
			? state.get(SteleBlock.TOP_FACING)
			: state.get(SteleBlock.BOTTOM_FACING);
	}

	public static SlabType getSlabType(BlockState state) {
		return state.get(TYPE);
	}

	public static boolean hasTop(BlockState state) {
		SlabType type = getSlabType(state);
		return type == SlabType.TOP || type == SlabType.DOUBLE;
	}

	public static boolean hasBottom(BlockState state) {
		SlabType type = getSlabType(state);
		return type == SlabType.BOTTOM || type == SlabType.DOUBLE;
	}

//	public Direction getRotation(BlockState state) {
//		return state.get(SteleBlock.TYPE) == SlabType.TOP
//			? state.get(SteleBlock.TOP_FACING)
//			: state.get(SteleBlock.BOTTOM_FACING);
//	}
//
//	public SlabType getSlabType(BlockState state) {
//		return state.get(TYPE);
//	}
//
//	public boolean hasTop(BlockState state) {
//		SlabType type = this.getSlabType(state);
//		return type == SlabType.TOP || type == SlabType.DOUBLE;
//	}
//
//	public boolean hasBottom(BlockState state) {
//		SlabType type = this.getSlabType(state);
//		return type == SlabType.BOTTOM || type == SlabType.DOUBLE;
//	}

	@Override
	public void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(TOP_FACING, BOTTOM_FACING, TYPE, ATTACHED, WATERLOGGED);
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
		if (existingState.isOf(this)) return this.updateExistingState(context, existingState);

		SlabType type = switch (context.getSide().getOpposite()) {
			case UP -> SlabType.TOP;
			case DOWN -> SlabType.BOTTOM;
			default -> context.getHitPos().y - context.getBlockPos().getY() > 0.5F ? SlabType.TOP : SlabType.BOTTOM;
		};
		boolean attached = context.getSide().getAxis().isHorizontal();
		boolean waterlogged = context.getWorld().getFluidState(pos).getFluid() == Fluids.WATER;

		BlockState state = this.getDefaultState()
			.with(TYPE, type)
			.with(ATTACHED, attached)
			.with(WATERLOGGED, waterlogged);

		Direction direction = attached ? context.getSide() : context.getHorizontalPlayerFacing().getOpposite();
		return switch (type) {
			case TOP -> state.with(TOP_FACING, direction);
			case BOTTOM -> state.with(BOTTOM_FACING, direction);
			default -> state;
		};
	}

	public BlockState updateExistingState(ItemPlacementContext context, BlockState state) {
		SlabType type = state.get(TYPE);
		if (type == SlabType.DOUBLE) return state;

		state = state.with(TYPE, SlabType.DOUBLE);
		Direction direction = context.getHorizontalPlayerFacing().getOpposite();
		return switch (type) {
			case TOP -> state.with(BOTTOM_FACING, direction);
			case BOTTOM -> state.with(TOP_FACING, direction);
			default -> state;
		};
	}

	@Override
	public boolean canReplace(BlockState state, ItemPlacementContext context) {
		if (state.isOf(this) && getSlabType(state) == SlabType.DOUBLE) return false;
		return context.getStack().isOf(this.asItem());
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
		return state
			.with(TOP_FACING, rotation.rotate(state.get(TOP_FACING)))
			.with(BOTTOM_FACING, rotation.rotate(state.get(BOTTOM_FACING)));
	}

	@Override
	public float getRotationDegrees(BlockState state) {
		return getRotation(state).asRotation();
	}

	public static float getRenderRotationDegrees(BlockState state) {
		Direction direction = getRotation(state);
		if (direction.getAxis() == Direction.Axis.X) direction = direction.getOpposite();
		return direction.asRotation();
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		SlabType type = state.get(TYPE);
		VoxelShape shape = VoxelShapes.empty();
		if (type == SlabType.TOP || type == SlabType.DOUBLE) shape = VoxelShapes.union(shape, getShapeForType(state, TOP_FACING, VERTICAL_OFFSET));
		if (type == SlabType.BOTTOM || type == SlabType.DOUBLE) shape = VoxelShapes.union(shape, getShapeForType(state, BOTTOM_FACING, 0));
		return shape.simplify();
	}

	public VoxelShape getShapeForType(BlockState state, DirectionProperty property, float offset) {
		VoxelShape shape = state.get(property).getAxis() == Direction.Axis.X ? X_SHAPE : Z_SHAPE;

		if (state.get(ATTACHED)) shape = switch (state.get(property)) {
			case NORTH -> shape.offset(0, 0, ATTACHED_OFFSET);
			case EAST -> shape.offset(-ATTACHED_OFFSET, 0, 0);
			case SOUTH -> shape.offset(0, 0, -ATTACHED_OFFSET);
			case WEST -> shape.offset(ATTACHED_OFFSET, 0, 0);
			default -> shape;
		};

		return shape.offset(0, offset, 0);
	}

}
