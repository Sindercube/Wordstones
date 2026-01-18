package org.sindercube.wordstones.content.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.sindercube.wordstones.registry.WordstonesComponentTypes;
import org.sindercube.wordstones.util.Location;

public class LocationBindingItem extends Item {

	public static float getLocationPredicate(ItemStack stack, World w, LivingEntity e, int s) {
		return hasLocation(stack) ? 1 : 0;
	}

	public LocationBindingItem(Item.Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		TypedActionResult<ItemStack> result = super.use(world, player, hand);
		if (result.getResult().isAccepted() || !player.isSneaking()) return result;

		ItemStack stack = player.getStackInHand(hand);
		Location location = stack.getOrDefault(WordstonesComponentTypes.LOCATION, Location.ZERO);
		if (location.isZero()) return result;

		stack.remove(WordstonesComponentTypes.LOCATION);
		return TypedActionResult.success(stack);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (context.getSide() == Direction.DOWN) return ActionResult.PASS;

		ItemPlacementContext placement = new ItemPlacementContext(context);
		BlockPos pos = placement.getBlockPos();
		World world = context.getWorld();
		if (world.getBlockState(pos.down()).isAir()) return ActionResult.PASS;

		ItemStack stack = context.getStack();
		Location location = Location.of(pos, world);
		if (getLocation(stack).equals(location)) return ActionResult.PASS;

		PlayerEntity player = context.getPlayer();
		if (player != null) {
			stack.set(WordstonesComponentTypes.LOCATION, location);
		} else {
			stack.decrement(1);
			ItemStack newStack = stack.copyWithCount(1);
			newStack.set(WordstonesComponentTypes.LOCATION, location);
			context.getPlayer().giveItemStack(newStack);
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public boolean hasGlint(ItemStack stack) {
		return stack.contains(WordstonesComponentTypes.LOCATION) || super.hasGlint(stack);
	}

//	@Override
//	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
//		Location location = stack.getOrDefault(WordstonesComponentTypes.LOCATION, Location.ZERO);
//		if (location.isZero()) return;
//
//		Vec3d vec = location.getVec3d();
//		Text text = Text.translatable("item.wordstones.bound_location",
//			location.getDimensionName(),
//			vec.x, vec.y, vec.z
//		).formatted(Formatting.GRAY);
//
//		tooltip.add(text);
//	}

	public static boolean hasLocation(ItemStack stack) {
		return stack.contains(WordstonesComponentTypes.LOCATION);
	}

	public static Location getLocation(ItemStack stack) {
		return stack.getOrDefault(WordstonesComponentTypes.LOCATION, Location.ZERO);
	}

}
