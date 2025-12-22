package org.sindercube.wordstones.content.packet;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.content.GlobalWordstones;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;

public record TeleportToWordstoneC2SPacket(BlockPos pos, Word word) implements CustomPayload {

	public static final Id<TeleportToWordstoneC2SPacket> ID = new Id<>(Wordstones.of("teleport_to_wordstone_c2s"));

	public static final PacketCodec<RegistryByteBuf, TeleportToWordstoneC2SPacket> CODEC = PacketCodec.tuple(
		BlockPos.PACKET_CODEC,
		TeleportToWordstoneC2SPacket::pos,
		Word.PACKET_CODEC,
		TeleportToWordstoneC2SPacket::word,
		TeleportToWordstoneC2SPacket::new
	);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public static void handle(TeleportToWordstoneC2SPacket packet, ServerPlayNetworking.Context context) {
		PlayerEntity player = context.player();
		World world = player.getWorld();
		if (!(world.getBlockEntity(packet.pos) instanceof WordstoneEntity wordstone)) return;
		if (wordstone.isPlayerTooFar(player)) return;
		if (!wordstone.hasWord()) return;
		System.out.println(GlobalWordstones.wordExists(world, packet.word));
		if (!GlobalWordstones.wordExists(world, packet.word)) return;

		GlobalWordstones.teleportToWordstone(player, packet.word);
	}

}
