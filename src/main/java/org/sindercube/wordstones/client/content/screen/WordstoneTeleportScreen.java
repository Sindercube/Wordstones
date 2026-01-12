package org.sindercube.wordstones.client.content.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;
import org.sindercube.wordstones.content.packet.WordstoneTeleportC2SPacket;

public class WordstoneTeleportScreen extends AbstractWordstoneScreen {

	public WordstoneTeleportScreen(WordstoneEntity wordstone) {
		super(Text.translatable("screen.wordstones.teleport_to_wordstone"), wordstone);
	}

	@Override
	public void onDone() {
		if (this.word.length() != 4) return;

		ClientPlayNetworking.send(new WordstoneTeleportC2SPacket(this.wordstone.getPos(), new Word(this.word)));
		this.close();
	}

}
