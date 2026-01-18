package org.sindercube.wordstones.client.content.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.text.Text;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;
import org.sindercube.wordstones.content.packet.WordstoneEditC2SPacket;

public class WordstoneEditScreen extends AbstractWordstoneScreen {

	public WordstoneEditScreen(WordstoneEntity wordstone) {
		super(Text.translatable("screen.wordstones.edit_wordstone"), wordstone);
	}

	@Override
	public void onDone() {
		super.onDone();
		ClientPlayNetworking.send(new WordstoneEditC2SPacket(this.wordstone.getPos(), new Word(this.word)));
	}

}
