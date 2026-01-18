package org.sindercube.wordstones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.sindercube.wordstones.content.packet.*;

public class WordstonesPackets {

	public static void init() {
		PayloadTypeRegistry.playC2S().register(WordstoneEditC2SPacket.ID, WordstoneEditC2SPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(WordstoneTeleportC2SPacket.ID, WordstoneTeleportC2SPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(EnchantedTomeTeleportC2SPacket.ID, EnchantedTomeTeleportC2SPacket.CODEC);

		PayloadTypeRegistry.playS2C().register(WordstoneEditS2CPacket.ID, WordstoneEditS2CPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(WordstoneTeleportS2CPacket.ID, WordstoneTeleportS2CPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(EnchantedTomeTeleportS2CPacket.ID, EnchantedTomeTeleportS2CPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(SteleEditS2CPacket.ID, SteleEditS2CPacket.CODEC);
	}

	@Environment(EnvType.CLIENT)
	public static void clientInit() {
		ServerPlayNetworking.registerGlobalReceiver(WordstoneEditC2SPacket.ID, WordstoneEditC2SPacket::handle);
		ServerPlayNetworking.registerGlobalReceiver(WordstoneTeleportC2SPacket.ID, WordstoneTeleportC2SPacket::handle);
		ServerPlayNetworking.registerGlobalReceiver(EnchantedTomeTeleportC2SPacket.ID, EnchantedTomeTeleportC2SPacket::handle);

		ClientPlayNetworking.registerGlobalReceiver(WordstoneEditS2CPacket.ID, WordstoneEditS2CPacket::handle);
		ClientPlayNetworking.registerGlobalReceiver(WordstoneTeleportS2CPacket.ID, WordstoneTeleportS2CPacket::handle);
		ClientPlayNetworking.registerGlobalReceiver(EnchantedTomeTeleportS2CPacket.ID, EnchantedTomeTeleportS2CPacket::handle);
		ClientPlayNetworking.registerGlobalReceiver(SteleEditS2CPacket.ID, SteleEditS2CPacket::handle);
	}

}
