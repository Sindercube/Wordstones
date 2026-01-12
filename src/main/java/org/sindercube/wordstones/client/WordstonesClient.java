package org.sindercube.wordstones.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import org.sindercube.wordstones.client.registry.WordstoneEntityRenderers;
import org.sindercube.wordstones.registry.WordstoneBlocks;
import org.sindercube.wordstones.registry.WordstoneItems;
import org.sindercube.wordstones.registry.WordstonePackets;

public class WordstonesClient implements ClientModInitializer {

	@Deprecated
	@Override
	public void onInitializeClient() {
		WordstoneEntityRenderers.init();
		WordstoneItems.clientInit();
		WordstoneBlocks.clientInit();
		WordstonePackets.clientInit();
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ModelPredicateProviderRegistry.init());

		WordstoneTypingEvent.WORD_TYPED.register(WordstonesClient::wordstoneEasterEggs);
	}

	public static void wordstoneEasterEggs(MinecraftClient client, String word) {
		if (word.equals("GSTR")) {
			if (client.world != null) client.world.disconnect();
			client.disconnect(new TitleScreen());
		}
	}

}
