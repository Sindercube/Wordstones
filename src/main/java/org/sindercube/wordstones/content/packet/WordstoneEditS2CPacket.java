package org.sindercube.wordstones.content.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.client.content.screen.WordstoneEditScreen;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;

public record WordstoneEditS2CPacket(BlockPos pos) implements CustomPayload {

	public static final CustomPayload.Id<WordstoneEditS2CPacket> ID = new CustomPayload.Id<>(Wordstones.of("wordstone_edit_s2c"));

	public static final PacketCodec<RegistryByteBuf, WordstoneEditS2CPacket> CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, WordstoneEditS2CPacket::pos,
		WordstoneEditS2CPacket::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Environment(EnvType.CLIENT)
	public static void handle(WordstoneEditS2CPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			if (context.player().getWorld().getBlockEntity(packet.pos) instanceof WordstoneEntity entity) {
				context.client().setScreen(new WordstoneEditScreen(entity));
			}
		});
	}

}
