package org.sindercube.wordstones.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;
import org.sindercube.wordstones.registry.WordstonesBlockEntityTypes;

public class WordstoneBlock extends AbstractWordstoneBlock {

	@Override
	protected MapCodec<? extends BlockWithEntity> getCodec() {
		return createCodec(WordstoneBlock::new);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		if (state.get(HALF) == DoubleBlockHalf.UPPER) return null;
		return new WordstoneEntity(pos, state);
	}

	public WordstoneBlock(Settings settings) {
		super(settings);
	}

	@Override
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onPlaced(world, pos, state, placer, stack);
		if (!(placer instanceof PlayerEntity player)) return;
		if (!(world.getBlockEntity(pos) instanceof WordstoneEntity wordstone)) return;

		this.openEditScreen(player, wordstone);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) return ActionResult.PASS;

		BlockPos entityPos = state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
		if (!(world.getBlockEntity(entityPos) instanceof WordstoneEntity wordstone)) return ActionResult.PASS;

		if (wordstone.hasWord()) {
			this.openTeleportScreen(player, wordstone);
		} else {
			this.openEditScreen(player, wordstone);
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? validateTicker(type, WordstonesBlockEntityTypes.WORDSTONE, WordstoneEntity::clientTick) : null;
	}

}
