package org.sindercube.wordstones.content.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CollisionView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sindercube.wordstones.GlobalWordstoneManager;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.content.block.WordstoneBlock;
import org.sindercube.wordstones.registry.WordstonesBlockEntityTypes;
import org.sindercube.wordstones.registry.WordstonesSoundEvents;
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

	public static boolean teleportToWordstone(ServerWorld serverWorld, PlayerEntity player, Word word) {
		Location location = GlobalWordstoneManager.get(serverWorld).getData().getOrDefault(word, Location.ZERO);
		if (location.isZero()) return false;
		return teleportToLocation(player, location);
	}

	public static boolean teleportToLocation(PlayerEntity player, Location location) {
		MinecraftServer server = player.getServer();
		if (server == null) return false;

		World world = location.getDimension(server);
		dropItems(world, location.pos(), player);

		BlockPos pos = location.pos();
		BlockState state = world.getBlockState(pos);
		Direction direction = state.get(WordstoneBlock.FACING);
		pos = pos.offset(direction);

//		player.setYaw(direction.asRotation());

//		Vec3d vec = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		Vec3d vec = findTeleportPosition(player.getType(), world, pos, direction, true);
		if (vec == null) {
			player.sendMessage(Text.translatable("message.wordstones.invalid_teleport"), true);
			return false;
		}

		if (player.hasVehicle()) player.stopRiding();
		player.setYaw(direction.getOpposite().asRotation());
		player.setPitch(0);

		world.playSound(null, player.getX(), player.getY(), player.getZ(),
			WordstonesSoundEvents.ENTITY_PLAYER_TELEPORT,
			SoundCategory.PLAYERS,
			1, 1
		);

		player.requestTeleport(vec.x, vec.y, vec.z);

		world.playSound(null, player.getX(), player.getY(), player.getZ(),
			WordstonesSoundEvents.ENTITY_PLAYER_TELEPORT,
			SoundCategory.PLAYERS,
			1, 1
		);
		return true;
	}

//	private Optional<Vec3d> findTeleportPosition(World world) {
//		BlockState state = world.getBlockState(this.getPos());
//		Direction direction = state.get(WordstoneBlock.FACING);
//		Vec3d start = this.getPos().offset(direction).toBottomCenterPos();
//
//
//	}

//	private static Optional<Vec3d> findWakeUpPosition(EntityType<?> type, CollisionView world, BlockPos pos, Direction bedDirection, Direction respawnDirection) {
//		int[][] is = getAroundBedOffsets(bedDirection, respawnDirection);
//		Optional<Vec3d> optional = findWakeUpPosition(type, world, pos, is, true);
//		if (optional.isPresent()) return optional;
//
//		BlockPos blockPos = pos.down();
//		Optional<Vec3d> optional2 = findWakeUpPosition(type, world, blockPos, is, true);
//		if (optional2.isPresent()) return optional2;
//
//		int[][] js = getOnBedOffsets(bedDirection);
//		Optional<Vec3d> optional3 = findWakeUpPosition(type, world, pos, js, true);
//		if (optional3.isPresent()) return optional3;
//
//		Optional<Vec3d> optional4 = findWakeUpPosition(type, world, pos, is, false);
//		if (optional4.isPresent()) return optional4;
//
//		Optional<Vec3d> optional5 = findWakeUpPosition(type, world, blockPos, is, false);
//		return optional5.isPresent() ? optional5 : findWakeUpPosition(type, world, pos, js, false);
//	}

//	private static int[][] getAroundBedOffsets(Direction bedDirection, Direction respawnDirection) {
//		return new int[][]{
//			{respawnDirection.getOffsetX(), respawnDirection.getOffsetZ()},
//			{respawnDirection.getOffsetX() - bedDirection.getOffsetX(), respawnDirection.getOffsetZ() - bedDirection.getOffsetZ()},
//			{respawnDirection.getOffsetX() - bedDirection.getOffsetX() * 2, respawnDirection.getOffsetZ() - bedDirection.getOffsetZ() * 2},
//			{-bedDirection.getOffsetX() * 2, -bedDirection.getOffsetZ() * 2},
//			{-respawnDirection.getOffsetX() - bedDirection.getOffsetX() * 2, -respawnDirection.getOffsetZ() - bedDirection.getOffsetZ() * 2},
//			{-respawnDirection.getOffsetX() - bedDirection.getOffsetX(), -respawnDirection.getOffsetZ() - bedDirection.getOffsetZ()},
//			{-respawnDirection.getOffsetX(), -respawnDirection.getOffsetZ()},
//			{-respawnDirection.getOffsetX() + bedDirection.getOffsetX(), -respawnDirection.getOffsetZ() + bedDirection.getOffsetZ()},
//			{bedDirection.getOffsetX(), bedDirection.getOffsetZ()},
//			{respawnDirection.getOffsetX() + bedDirection.getOffsetX(), respawnDirection.getOffsetZ() + bedDirection.getOffsetZ()}};
//	}

//	private static Optional<Vec3d> findWakeUpPosition(EntityType<?> type, CollisionView world, BlockPos pos, int[][] possibleOffsets, boolean ignoreInvalidPos) {
//		BlockPos.Mutable mutable = new BlockPos.Mutable();
//
//		for(int[] is : possibleOffsets) {
//			mutable.set(pos.getX() + is[0], pos.getY(), pos.getZ() + is[1]);
//			Vec3d vec3d = Dismounting.findRespawnPos(type, world, mutable, ignoreInvalidPos);
//			if (vec3d != null) {
//				return Optional.of(vec3d);
//			}
//		}
//
//		return Optional.empty();
//	}

	@Nullable
	public static Vec3d findTeleportPosition(EntityType<?> entityType, CollisionView world, BlockPos pos, Direction direction, boolean ignoreInvalidPos) {
		Iterable<BlockPos.Mutable> positions = BlockPos.iterateInSquare(pos, 1, direction, direction.getOpposite());
		for (BlockPos.Mutable mutable : positions) {
			Vec3d position = tryFindTeleportPosition(entityType, world, mutable, ignoreInvalidPos);
			if (position == null) continue;
			return position;
		}
		return null;
	}

	@Nullable
	public static Vec3d tryFindTeleportPosition(EntityType<?> entityType, CollisionView world, BlockPos pos, boolean ignoreInvalidPos) {
		if (ignoreInvalidPos && entityType.isInvalidSpawn(world.getBlockState(pos))) return null;

		double height = world.getDismountHeight(Dismounting.getCollisionShape(world, pos), () -> Dismounting.getCollisionShape(world, pos.down()));
		if (!Dismounting.canDismountInBlock(height)) return null;
		if (ignoreInvalidPos && height <= (double)0.0F && entityType.isInvalidSpawn(world.getBlockState(pos.down()))) return null;

		Vec3d result = Vec3d.ofCenter(pos, height);
		Box box = entityType.getDimensions().getBoxAt(result);

		for (VoxelShape shape : world.getBlockCollisions(null, box)) {
			if (!shape.isEmpty()) return null;
		}

		if (entityType != EntityType.PLAYER || !world.getBlockState(pos).isIn(BlockTags.INVALID_SPAWN_INSIDE) && !world.getBlockState(pos.up()).isIn(BlockTags.INVALID_SPAWN_INSIDE)) {
			return !world.getWorldBorder().contains(box) ? null : result;
		}
		return null;
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
