package org.sindercube.wordstones.content.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;
import org.sindercube.wordstones.content.state.GlobalWordstoneManager;

public record EnchantedTomeTeleportC2SPacket(Hand hand, Word word) implements CustomPayload {

	public static final Id<EnchantedTomeTeleportC2SPacket> ID = new Id<>(Wordstones.of("enchanted_tome_teleport_c2s"));

	public static final PacketCodec<RegistryByteBuf, EnchantedTomeTeleportC2SPacket> CODEC = PacketCodec.tuple(
		EnchantedTomeTeleportS2CPacket.HAND_CODEC, EnchantedTomeTeleportC2SPacket::hand,
		Word.PACKET_CODEC, EnchantedTomeTeleportC2SPacket::word,
		EnchantedTomeTeleportC2SPacket::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public static void handle(EnchantedTomeTeleportC2SPacket packet, ServerPlayNetworking.Context context) {
		context.server().execute(() -> {
			ServerWorld world = context.server().getOverworld();
			PlayerEntity player = context.player();

			if (!GlobalWordstoneManager.get(world).getData().containsKey(packet.word)) {
				player.sendMessage(Text.translatable("message.wordstones.not_found"), true);
				return;
			}

			WordstoneEntity.teleportToWordstone(world, player, packet.word);
			player.getStackInHand(packet.hand).decrementUnlessCreative(1, player);
		});
	}

}
