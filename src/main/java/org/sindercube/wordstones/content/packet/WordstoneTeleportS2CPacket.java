package org.sindercube.wordstones.content.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.client.content.screen.WordstoneTeleportScreen;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;

public record WordstoneTeleportS2CPacket(BlockPos pos) implements CustomPayload {

	public static final Id<WordstoneTeleportS2CPacket> ID = new Id<>(Wordstones.of("wordstone_teleport_s2c"));

	public static final PacketCodec<RegistryByteBuf, WordstoneTeleportS2CPacket> CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, WordstoneTeleportS2CPacket::pos,
		WordstoneTeleportS2CPacket::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Environment(EnvType.CLIENT)
	public void handle(ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			if (context.player().getWorld().getBlockEntity(this.pos) instanceof WordstoneEntity wordstone) {
				context.client().setScreen(new WordstoneTeleportScreen(wordstone));
			}
		});
	}

}
