package org.sindercube.wordstones.client.content.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.content.packet.EnchantedTomeTeleportC2SPacket;

public class EnchantedTomeTeleportScreen extends AbstractWordstoneScreen {

	public final Hand hand;

	public EnchantedTomeTeleportScreen(Hand hand) {
		super(Text.translatable("screen.wordstones.teleport_to_wordstone"), null);
		this.hand = hand;
	}

	@Override
	public void tick() {}

	@Override
	public void onDone() {
		super.onDone();
		ClientPlayNetworking.send(new EnchantedTomeTeleportC2SPacket(this.hand, new Word(this.word)));
	}

}
