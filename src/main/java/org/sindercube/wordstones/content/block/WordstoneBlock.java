package org.sindercube.wordstones.content.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;
import org.sindercube.wordstones.content.packet.EditWordstoneS2CPacket;
import org.sindercube.wordstones.content.packet.TeleportToWordstoneS2CPacket;
import org.sindercube.wordstones.registry.WordstoneBlockEntityTypes;

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
	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
		BlockPos abovePos = pos.up();
		world.setBlockState(abovePos, withWaterloggedState(world, abovePos, this.getDefaultState().with(HALF, DoubleBlockHalf.UPPER)));

		this.openEditScreen(world, pos, placer);
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (world.isClient) return ActionResult.PASS;

		BlockPos entityPos = state.get(HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;

		if (!(world.getBlockEntity(entityPos) instanceof WordstoneEntity wordstone)) return ActionResult.PASS;
		if (!wordstone.hasWord()) {
			this.openEditScreen(world, entityPos, player);
			return ActionResult.SUCCESS;
		}

		this.openTeleportScreen(world, entityPos, player);
		return ActionResult.SUCCESS;
	}

	public void openEditScreen(World world, BlockPos pos, LivingEntity entity) {
		if (world.isClient) return;
		if (!(entity instanceof ServerPlayerEntity player)) return;

		ServerPlayNetworking.send(player, new EditWordstoneS2CPacket(pos));
	}

	public void openTeleportScreen(World world, BlockPos pos, LivingEntity entity) {
		if (world.isClient) return;
		if (!(entity instanceof ServerPlayerEntity player)) return;

		ServerPlayNetworking.send(player, new TeleportToWordstoneS2CPacket(pos));
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return world.isClient ? validateTicker(type, WordstoneBlockEntityTypes.WORDSTONE, WordstoneEntity::tick) : null;
	}

}
