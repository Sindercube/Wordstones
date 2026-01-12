package org.sindercube.wordstones.content.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.client.content.screen.SteleEditScreen;
import org.sindercube.wordstones.content.block.entity.SteleEntity;

public record SteleEditS2CPacket(
	BlockPos pos,
	boolean front
) implements CustomPayload {

	public static final Id<SteleEditS2CPacket> ID = new Id<>(Wordstones.of("stele_edit_s2c"));

	public static final PacketCodec<RegistryByteBuf, SteleEditS2CPacket> CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, SteleEditS2CPacket::pos,
		PacketCodecs.BOOL, SteleEditS2CPacket::front,
		SteleEditS2CPacket::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Environment(EnvType.CLIENT)
	public static void handle(SteleEditS2CPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			if (context.player().getWorld().getBlockEntity(packet.pos) instanceof SteleEntity entity) {
				context.client().setScreen(new SteleEditScreen(entity, packet.front, context.client().shouldFilterText()));
			}
		});
	}

}
