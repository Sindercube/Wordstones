package org.sindercube.wordstones.content.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.sindercube.wordstones.GlobalWordstoneManager;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;

public record WordstoneEditC2SPacket(BlockPos pos, Word word) implements CustomPayload {

	public static final Id<WordstoneEditC2SPacket> ID = new Id<>(Wordstones.of("wordstone_edit_c2s"));

	public static final PacketCodec<RegistryByteBuf, WordstoneEditC2SPacket> CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC, WordstoneEditC2SPacket::pos,
		Word.PACKET_CODEC, WordstoneEditC2SPacket::word,
		WordstoneEditC2SPacket::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public static class Handler {

		public static void handle(WordstoneEditC2SPacket packet, ServerPlayNetworking.Context context) {
			context.server().execute(() -> {
				ServerWorld world = context.server().getOverworld();
				PlayerEntity player = context.player();
				if (!(world.getBlockEntity(packet.pos) instanceof WordstoneEntity wordstone)) return;

				if (wordstone.isPlayerTooFar(player)) {
					player.sendMessage(Text.translatable("message.wordstones.player_too_far").formatted(Formatting.RED), true);
					return;
				}
				if (wordstone.hasWord()) {
					player.sendMessage(Text.translatable("message.wordstones.already_has_word").formatted(Formatting.RED), true);
					return;
				}

				if (GlobalWordstoneManager.get(world).getData().containsKey(packet.word)) {
					player.sendMessage(Text.translatable("message.wordstones.word_exists"), true);
					return;
				}

				wordstone.setWord(packet.word);
			});
		}

	}

}
