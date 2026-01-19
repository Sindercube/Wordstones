package org.sindercube.wordstones.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.sindercube.wordstones.registry.*;

public class WordstonesClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		WordstonesEntityTypes.clientInit();
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
