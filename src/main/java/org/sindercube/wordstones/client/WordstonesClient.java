package org.sindercube.wordstones.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.sindercube.wordstones.client.registry.WordstoneEntityRenderers;
import org.sindercube.wordstones.registry.WordstonesBlocks;
import org.sindercube.wordstones.registry.WordstonesItems;
import org.sindercube.wordstones.registry.WordstonesPackets;
import org.sindercube.wordstones.registry.WordstonesParticleTypes;

public class WordstonesClient implements ClientModInitializer {

	@Deprecated
	@Override
	public void onInitializeClient() {
		WordstoneEntityRenderers.init();
		WordstonesItems.clientInit();
		WordstonesBlocks.clientInit();
		WordstonesPackets.clientInit();
		WordstonesParticleTypes.clientInit();

		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ModelPredicateProviderRegistry.init());
		WordstoneTypingEvent.WORD_TYPED.register(WordstonesClient::wordstoneEasterEggs);
	}

	public static void wordstoneEasterEggs(MinecraftClient client, String word) {
		switch (word) {
			case "GSTR" -> {
				if (client.world != null) client.world.disconnect();
				client.disconnect(new TitleScreen());
			}
		}
	}

}
