package org.sindercube.wordstones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import org.sindercube.wordstones.content.packet.*;

public class WordstonePackets {

	public static void init() {
		PayloadTypeRegistry.playS2C().register(EditWordstoneS2CPacket.ID, EditWordstoneS2CPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(EditWordstoneC2SPacket.ID, EditWordstoneC2SPacket.CODEC);
		PayloadTypeRegistry.playS2C().register(TeleportToWordstoneS2CPacket.ID, TeleportToWordstoneS2CPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(TeleportToWordstoneC2SPacket.ID, TeleportToWordstoneC2SPacket.CODEC);
	}

	@Environment(EnvType.CLIENT)
	public static void clientInit() {
		ClientPlayNetworking.registerGlobalReceiver(EditWordstoneS2CPacket.ID, EditWordstoneS2CPacket::handle);
		ServerPlayNetworking.registerGlobalReceiver(EditWordstoneC2SPacket.ID, EditWordstoneC2SPacket::handle);
		ClientPlayNetworking.registerGlobalReceiver(TeleportToWordstoneS2CPacket.ID, TeleportToWordstoneS2CPacket::handle);
		ServerPlayNetworking.registerGlobalReceiver(TeleportToWordstoneC2SPacket.ID, TeleportToWordstoneC2SPacket::handle);
	}

}
