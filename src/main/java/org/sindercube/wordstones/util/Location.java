package org.sindercube.wordstones.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public record Location (
	BlockPos pos,
	RegistryKey<World> worldKey
) {

	public static final Codec<Location> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BlockPos.CODEC.fieldOf("position").forGetter(Location::pos),
		World.CODEC.fieldOf("world").forGetter(Location::worldKey)
	).apply(instance, Location::new));

	public static final PacketCodec<ByteBuf, RegistryKey<World>> WORLD_PACKET_CODEC = RegistryKey.createPacketCodec(RegistryKeys.WORLD);

	public static final PacketCodec<RegistryByteBuf, Location> PACKET_CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, Location::pos,
		WORLD_PACKET_CODEC, Location::worldKey,
		Location::new
	);

	public static Location of(BlockPos pos, World world) {
		return new Location(pos, world.getRegistryKey());
	}

	public static final Location ZERO = new Location(
		new BlockPos(0, 0, 0),
		RegistryKey.of(RegistryKeys.WORLD, Identifier.ofVanilla("null"))
	);

	public boolean isZero() {
		return this.equals(ZERO);
	}

	public Vec3d getVec() {
		return Vec3d.of(this.pos);
	}

	public String dimensionId() {
		return this.worldKey.getValue().toTranslationKey("dimension");
	}

	public Text dimensionName() {
		return Text.translatable(dimensionId());
	}

}
