package org.sindercube.wordstones.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.MinecraftClient;

public class WordstoneTypingEvent {

	public static final Event<WordTyped> WORD_TYPED = EventFactory.createArrayBacked(WordTyped.class,
		callbacks -> (client, word) -> {
			for (WordTyped callback : callbacks) callback.wordTyped(client, word);
	});

	@FunctionalInterface
	public interface WordTyped {
		void wordTyped(MinecraftClient client, String word);
	}

}
