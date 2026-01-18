package org.sindercube.wordstones;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.util.Identifier;
import org.sindercube.wordstones.content.entity.EnchantedSquidEntity;
import org.sindercube.wordstones.registry.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Wordstones implements ModInitializer {

	public static final String MOD_ID = "wordstones";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier of(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		WordstonesComponentTypes.init();

		WordstonesBlocks.init();
		WordstonesItems.init();
		WordstonesEntityTypes.init();
		WordstonesBlockEntityTypes.init();

		WordstonesItemGroups.init();
		WordstonesSoundEvents.init();
		WordstonesPackets.init();
		WordstonesTags.init();

		WordstonesCommands.init();
		WordstonesParticleTypes.init();

		LootTableEvents.MODIFY.register(WordstonesLootTableChanges::modifyLootTables);
		UseEntityCallback.EVENT.register(EnchantedSquidEntity::tryTransformSquid);
	}

}
