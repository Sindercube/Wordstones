package org.sindercube.wordstones.content.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sindercube.wordstones.GlobalWordstoneManager;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.content.block.WordstoneBlock;
import org.sindercube.wordstones.registry.WordstonesBlockEntityTypes;
import org.sindercube.wordstones.registry.WordstonesTags;
import org.sindercube.wordstones.util.Location;

public class WordstoneEntity extends BlockEntity {

	private static final Random RANDOM = Random.create();

	@Nullable protected Word word;
	public RenderState renderState = new RenderState();

	public WordstoneEntity(BlockPos pos, BlockState state) {
        super(WordstonesBlockEntityTypes.WORDSTONE, pos, state);
    }

	@Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("word")) this.word = new Word(nbt.getString("word"));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (this.word != null) nbt.putString("word", this.word.value());
    }

	public boolean hasWord() {
		return this.word != null;
	}

	@Nullable
    public Word getWord() {
        return this.word;
    }

    public void setWord(Word word) {
        this.word = word;
		markDirty();
		if (this.world != null && this.world instanceof ServerWorld serverWorld) {
			GlobalWordstoneManager.get(serverWorld).set(word, new Location(this.getPos(), serverWorld.getRegistryKey()));
		}
	}

	@Override
    public void markRemoved() {
		if (this.world != null && this.world instanceof ServerWorld serverWorld && serverWorld.isChunkLoaded(new ChunkPos(this.getPos()).toLong())) {
			GlobalWordstoneManager.get(serverWorld).remove(word);
		}
		super.markRemoved();
    }

	public boolean isPlayerTooFar(PlayerEntity player) {
		return !player.canInteractWithBlockAt(this.getPos(), 4);
	}

	public static void clientTick(World world, BlockPos pos, BlockState state, WordstoneEntity entity) {
		RenderState renderState = entity.renderState;
		renderState.pageTurningSpeed = renderState.nextPageTurningSpeed;
		renderState.lastBookRotation = renderState.bookRotation;
		PlayerEntity player = world.getClosestPlayer((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, 3, false);
		if (player != null) {
			double d = player.getX() - ((double)pos.getX() + (double)0.5F);
			double e = player.getZ() - ((double)pos.getZ() + (double)0.5F);
			renderState.targetBookRotation = (float) MathHelper.atan2(e, d);
			renderState.nextPageTurningSpeed += 0.1F;
			if (renderState.nextPageTurningSpeed < 0.5F || RANDOM.nextInt(40) == 0) {
				float f = renderState.flipRandom;

				while (f == renderState.flipRandom) {
					renderState.flipRandom += (float)(RANDOM.nextInt(4) - RANDOM.nextInt(4));
				}
			}
		} else {
			renderState.targetBookRotation += 0.02F;
			renderState.nextPageTurningSpeed -= 0.1F;
		}

		while (renderState.bookRotation >= (float) Math.PI) {
			renderState.bookRotation -= (float)Math.PI * 2;
		}

		while (renderState.bookRotation < -(float) Math.PI) {
			renderState.bookRotation += (float)Math.PI * 2;
		}

		while (renderState.targetBookRotation >= (float) Math.PI) {
			renderState.targetBookRotation -= (float)Math.PI * 2;
		}

		while (renderState.targetBookRotation < -(float) Math.PI) {
			renderState.targetBookRotation += (float)Math.PI * 2;
		}

		float rotation = renderState.targetBookRotation - renderState.bookRotation;
		if (rotation >= (float) Math.PI) {
			rotation -= (float) Math.PI * 2;
		}

		while (rotation < -(float)Math.PI) {
			rotation += (float)Math.PI * 2;
		}

		renderState.bookRotation += rotation * 0.4F;
		renderState.nextPageTurningSpeed = MathHelper.clamp(renderState.nextPageTurningSpeed, 0, 1);
		++renderState.ticks;
		renderState.pageAngle = renderState.nextPageAngle;
		float flipTurn = (renderState.flipRandom - renderState.nextPageAngle) * 0.4F;
		flipTurn = MathHelper.clamp(flipTurn, -0.2F, 0.2F);
		renderState.flipTurn += (flipTurn - renderState.flipTurn) * 0.9F;
		renderState.nextPageAngle += renderState.flipTurn;
	}

	public static void teleportToWordstone(ServerWorld serverWorld, PlayerEntity player, Word word) {
		Location location = GlobalWordstoneManager.get(serverWorld).getData().getOrDefault(word, Location.ZERO);
		if (!location.isZero()) teleportToLocation(player, location);
	}

	public static void teleportToLocation(PlayerEntity player, Location location) {
		MinecraftServer server = player.getServer();
		if (server == null) return;

		World locationWorld = location.getDimension(server);
		BlockPos pos = location.pos();
		BlockState state = locationWorld.getBlockState(pos);
		Direction direction = state.get(WordstoneBlock.FACING);
		pos = pos.offset(direction, 2);

//		player.setYaw(direction.asRotation());
		dropItems(locationWorld, location.pos(), player);

		Vec3d vec = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		if (player.hasVehicle()) player.stopRiding();
		player.setYaw(direction.getOpposite().asRotation());
		player.setPitch(0);
		player.requestTeleport(vec.x, vec.y, vec.z);

		locationWorld.playSound(null, player.getX(), player.getY(), player.getZ(),
			SoundEvents.ENTITY_ENDERMAN_TELEPORT,
			SoundCategory.PLAYERS,
			1, 1
		);
	}

	public static void dropItems(World world, BlockPos pos, PlayerEntity player) {
		if (player.isCreative()) return;

		for (Direction direction : Direction.Type.HORIZONTAL) {
			BlockEntity entity = world.getBlockEntity(pos.offset(direction));
			if (entity instanceof DropBoxEntity dropBox && !dropBox.hasInventoryForPlayer(player)) {
				dropBox.depositItems(player);
				return;
			}
		}

		if (player.getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) return;

		for (int i = 0; i < player.getInventory().size(); ++i) {
			ItemStack stack = player.getInventory().getStack(i);
			if (stack.isEmpty()) continue;
			if (EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
				player.getInventory().removeStack(i);
				continue;
			}
			if (stack.isIn(WordstonesTags.KEPT_ACROSS_TELEPORTATION)) continue;
			player.dropItem(stack, true, true);
			player.getInventory().removeStack(i);
		}
	}

	public static class RenderState {

		public int ticks;
		public float nextPageAngle;
		public float pageAngle;
		public float flipRandom;
		public float flipTurn;
		public float nextPageTurningSpeed;
		public float pageTurningSpeed;
		public float bookRotation;
		public float lastBookRotation;
		public float targetBookRotation;

	}

}
