package org.sindercube.wordstones.content;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.solstice.euclidsElements.util.Location;
import org.sindercube.wordstones.content.block.WordstoneBlock;
import org.sindercube.wordstones.content.block.entity.DropBoxEntity;
import org.sindercube.wordstones.registry.WordstoneAttachmentTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class GlobalWordstones {

	public static final Codec<Map<Word, Location>> CODEC = Codec.unboundedMap(
		Word.CODEC,
		Location.CODEC
	);

	public static final PacketCodec<RegistryByteBuf, Map<Word, Location>> PACKET_CODEC = PacketCodecs.map(
		HashMap::new,
		Word.PACKET_CODEC,
		Location.PACKET_CODEC
	);

	public static Map<Word, Location> getGlobalWordstones(World world) {
		return world.getAttachedOrCreate(WordstoneAttachmentTypes.GLOBAL_WORDSTONES, HashMap::new);
	}

	public static void modifyGlobalWordstones(World world, UnaryOperator<Map<Word, Location>> function) {
		world.modifyAttached(WordstoneAttachmentTypes.GLOBAL_WORDSTONES, function);
	}

	public static void addGlobalWordstone(World world, Word word, Location location) {
		modifyGlobalWordstones(world, map -> {
			map = new HashMap<>(map);
			map.put(word, location);
			return map;
		});
	}

	public static void removeGlobalWordstone(World world, Word word) {
		modifyGlobalWordstones(world, map -> {
			map = new HashMap<>(map);
			map.remove(word);
			return map;
		});
	}

	public static boolean wordExists(World world, Word word) {
		return getGlobalWordstones(world).containsKey(word);
	}

	public static void teleportToWordstone(PlayerEntity player, Word word) {
		Location location = getGlobalWordstones(player.getWorld()).getOrDefault(word, Location.ZERO);
		if (location.isZero()) return;

		World world = player.getWorld().getServer().getWorld(location.worldKey());
		BlockPos pos = location.pos();
		BlockState state = world.getBlockState(pos);
		Direction direction = state.get(WordstoneBlock.FACING);
		pos = pos.offset(direction);

		dropItems(world, location.pos(), player);

		Vec3d vec = new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
		if (player.hasVehicle()) player.stopRiding();
		player.requestTeleport(vec.x, vec.y, vec.z);
		player.setYaw(direction.asRotation());

		world.playSound(null, player.getX(), player.getY(), player.getZ(),
			SoundEvents.ENTITY_ENDERMAN_TELEPORT,
			SoundCategory.PLAYERS,
			1, 1
		);
	}

	public static void dropItems(World world, BlockPos pos, PlayerEntity player) {
		if (player.isCreative()) return;

		for (Direction direction : Direction.Type.HORIZONTAL) {
			BlockEntity entity = world.getBlockEntity(pos.offset(direction));
			if (entity instanceof DropBoxEntity dropBox && !dropBox.hasPlayerInventory(player)) {
				dropBox.depositItems(player);
				return;
			}
		}

		player.dropInventory();
	}

}
