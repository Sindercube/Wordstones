package org.sindercube.wordstones.content.packet;

import io.netty.buffer.ByteBuf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Hand;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.client.content.screen.EnchantedTomeTeleportScreen;

public record EnchantedTomeTeleportS2CPacket(Hand hand) implements CustomPayload {

	public static final Id<EnchantedTomeTeleportS2CPacket> ID = new Id<>(Wordstones.of("enchanted_tome_teleport_s2c"));

	public static final PacketCodec<ByteBuf, Hand> HAND_CODEC = PacketCodecs.BOOL.xmap(
		bool -> bool ? Hand.MAIN_HAND : Hand.OFF_HAND,
		hand -> hand == Hand.MAIN_HAND
	);

	public static final PacketCodec<RegistryByteBuf, EnchantedTomeTeleportS2CPacket> CODEC = PacketCodec.tuple(
		HAND_CODEC, EnchantedTomeTeleportS2CPacket::hand,
		EnchantedTomeTeleportS2CPacket::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	@Environment(EnvType.CLIENT)
	public static void handle(EnchantedTomeTeleportS2CPacket packet, ClientPlayNetworking.Context context) {
		context.client().execute(() -> context.client().setScreen(new EnchantedTomeTeleportScreen(packet.hand)));
	}

}
