package org.sindercube.wordstones.content.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.content.GlobalWordstones;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;

public record EditWordstoneC2SPacket(BlockPos pos, Word word) implements CustomPayload {

	public static final CustomPayload.Id<EditWordstoneC2SPacket> ID = new CustomPayload.Id<>(Wordstones.of("update_wordstone_c2s"));
	public static final Text WORD_EXISTS = Text.translatable("message.wordstones.word_exists");

	public static final PacketCodec<RegistryByteBuf, EditWordstoneC2SPacket> CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC,
		EditWordstoneC2SPacket::pos,
		Word.PACKET_CODEC,
		EditWordstoneC2SPacket::word,
		EditWordstoneC2SPacket::new
	);

	@Override
	public CustomPayload.Id<? extends CustomPayload> getId() {
		return ID;
	}

	public static void handle(EditWordstoneC2SPacket packet, ServerPlayNetworking.Context context) {
		PlayerEntity player = context.player();
		World world = player.getWorld();
		if (!(world.getBlockEntity(packet.pos) instanceof WordstoneEntity wordstone)) return;
		if (wordstone.isPlayerTooFar(player)) return;
		if (wordstone.hasWord()) return;

		if (GlobalWordstones.wordExists(world, packet.word)) {
			context.player().sendMessage(WORD_EXISTS, true);
			return;
		}

		wordstone.setWord(packet.word);
	}

}
