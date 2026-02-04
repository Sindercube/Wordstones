package org.sindercube.wordstones;

import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.CollisionView;
import net.minecraft.world.GameRules;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.content.block.WordstoneBlock;
import org.sindercube.wordstones.content.block.entity.DropBoxEntity;
import org.sindercube.wordstones.registry.WordstonesSoundEvents;
import org.sindercube.wordstones.registry.WordstonesTags;
import org.sindercube.wordstones.util.Location;

import java.util.HashMap;
import java.util.Map;

public class GlobalWordstoneManager extends PersistentState {

	public static final Codec<Map<Word, Location>> CODEC = Codec.unboundedMap(Word.CODEC, Location.CODEC);

	public static GlobalWordstoneManager get(ServerWorld world) {
		return world.getServer().getOverworld().getPersistentStateManager().getOrCreate(
			GlobalWordstoneManager.getPersistentStateType(), "wordstones:global_wordstones"
		);
	}

	public static Type<GlobalWordstoneManager> getPersistentStateType() {
		return new Type<>(
			GlobalWordstoneManager::new,
			GlobalWordstoneManager::fromNbt,
			null
		);
	}

	protected final Map<Word, Location> data;

	public GlobalWordstoneManager() {
		this.data = new HashMap<>();
	}

	public static boolean teleportToWordstone(ServerWorld serverWorld, PlayerEntity player, Word word) {
		Location location = get(serverWorld).getData().getOrDefault(word, Location.ZERO);
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

	public Map<Word, Location> getData() {
		return this.data;
	}

	public void set(Word word, Location location) {
		this.data.put(word, location);
		this.markDirty();
	}

	public void remove(Word word) {
		this.data.remove(word);
		this.markDirty();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		return (NbtCompound) CODEC.encodeStart(NbtOps.INSTANCE, this.data).getOrThrow();
	}

	public static GlobalWordstoneManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		GlobalWordstoneManager manager = new GlobalWordstoneManager();
		manager.data.putAll(CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow());
		return manager;
	}

}
