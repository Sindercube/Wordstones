package org.sindercube.wordstones.content.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.content.block.WordstoneBlock;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;
import org.sindercube.wordstones.registry.WordstonesBlocks;
import org.sindercube.wordstones.registry.WordstonesComponentTypes;
import org.sindercube.wordstones.registry.WordstonesSoundEvents;
import org.sindercube.wordstones.util.Location;

public class TomeItem extends Item {

	public TomeItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);

		Location location = stack.getOrDefault(WordstonesComponentTypes.LOCATION, Location.ZERO);
		if (location.isZero()) return TypedActionResult.pass(stack);

		stack.decrementUnlessCreative(1, player);
		WordstoneEntity.teleportToLocation(player, location);
		return TypedActionResult.success(stack);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		PlayerEntity player = context.getPlayer();
		if (player != null && !player.isSneaking()) return super.useOnBlock(context);

		World world = context.getWorld();
		BlockPos pos = context.getBlockPos();
		BlockState state = world.getBlockState(pos);
		if (!state.isOf(WordstonesBlocks.WORDSTONE)) return super.useOnBlock(context);

		BlockPos entityPos = state.get(WordstoneBlock.HALF) == DoubleBlockHalf.UPPER ? pos.down() : pos;
		WordstoneEntity entity = (WordstoneEntity) world.getBlockEntity(entityPos);
		if (entity == null) return super.useOnBlock(context);

		ItemStack stack = context.getStack();
		Location location = new Location(entityPos, world.getRegistryKey());

		if (!player.isInCreativeMode() && stack.getCount() == 1) {
			stack.set(WordstonesComponentTypes.LOCATION, location);
		} else {
			ItemStack copy = stack.copyWithCount(1);
			stack.decrementUnlessCreative(1, player);
			copy.set(WordstonesComponentTypes.LOCATION, location);
			if (!player.getInventory().insertStack(copy)) player.dropItem(copy, false);
		}
		world.playSound(null, pos, WordstonesSoundEvents.ITEM_LINKED_TOME_LINK, SoundCategory.PLAYERS, 1, 1);

		return ActionResult.SUCCESS;
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return stack.contains(WordstonesComponentTypes.LOCATION) || super.hasGlint(stack);
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		return stack.contains(WordstonesComponentTypes.LOCATION) ? "item." + Wordstones.MOD_ID + ".linked_tome" : super.getTranslationKey(stack);
	}

}
