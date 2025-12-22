package org.sindercube.wordstones.content.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.client.content.screen.WordstoneTeleportScreen;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;

public record TeleportToWordstoneS2CPacket(BlockPos pos) implements CustomPayload {

	public static final Id<TeleportToWordstoneS2CPacket> ID = new Id<>(Wordstones.of("teleport_to_wordstone_s2c"));

	public static final PacketCodec<RegistryByteBuf, TeleportToWordstoneS2CPacket> CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC,
		TeleportToWordstoneS2CPacket::pos,
		TeleportToWordstoneS2CPacket::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Environment(EnvType.CLIENT)
	public static void handle(TeleportToWordstoneS2CPacket packet, ClientPlayNetworking.Context context) {
		World world = context.player().getWorld();
		if (world.getBlockEntity(packet.pos) instanceof WordstoneEntity wordstone)
			context.client().setScreen(new WordstoneTeleportScreen(wordstone));
	}

}
