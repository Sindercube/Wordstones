package org.sindercube.wordstones;

import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sindercube.wordstones.registry.*;

public class Wordstones implements ModInitializer {

	public static final String MOD_ID = "wordstones";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier of(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		WordstoneComponentTypes.init();

		WordstoneBlocks.init();
		WordstoneItems.init();
		WordstoneBlockEntityTypes.init();

		WordstoneItemGroups.init();
		WordstonesSoundEvents.init();
		WordstonePackets.init();
		WordstoneTags.init();

		WordstoneCommands.init();
	}

}
