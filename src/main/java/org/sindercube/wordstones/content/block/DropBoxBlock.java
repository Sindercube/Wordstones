package org.sindercube.wordstones.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import org.sindercube.wordstones.content.block.entity.DropBoxEntity;
import org.sindercube.wordstones.registry.WordstonesSoundEvents;

public class DropBoxBlock extends BlockWithEntity {

	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	public static final VoxelShape X_SHAPE = Block.createCuboidShape(2, 0, 0, 14, 16, 16);
	public static final VoxelShape Z_SHAPE = Block.createCuboidShape(0, 0, 2, 16, 16, 14);

	@Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(DropBoxBlock::new);
    }

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new DropBoxEntity(pos, state);
	}

    public DropBoxBlock(Settings settings) {
        super(settings);
		this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		super.appendProperties(builder);
		builder.add(FACING);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof DropBoxEntity dropBox)) return ActionResult.PASS;

		if (player.isSneaking() && player.getMainHandStack().isEmpty()) {
			ActionResult result = dropBox.depositItems(player);
			if (result.isAccepted()) {
				world.playSound(player, pos.getX(), pos.getY(), pos.getZ(), WordstonesSoundEvents.DROP_BOX_DEPOSIT, SoundCategory.PLAYERS, 1, 1);
				world.emitGameEvent(player,  GameEvent.BLOCK_ACTIVATE, pos);
			}
			return result;
		}

		ActionResult result = dropBox.retrieveItems(player);
		if (result.isAccepted()) {
			world.playSound(player, pos.getX(), pos.getY(), pos.getZ(), WordstonesSoundEvents.DROP_BOX_RETRIEVE, SoundCategory.PLAYERS, 1, 1);
			world.emitGameEvent(player, GameEvent.BLOCK_DEACTIVATE, pos);
		}
		return result;
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext context) {
		return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
	}

	@Override
	protected BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (state.get(FACING).getAxis() == Direction.Axis.X) return X_SHAPE;
		return Z_SHAPE;
	}

	@Override
	protected BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

}
