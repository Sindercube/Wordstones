package org.sindercube.wordstones;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.sindercube.wordstones.content.entity.EnchantedSquidEntity;
import org.sindercube.wordstones.content.item.LastWillItem;
import org.sindercube.wordstones.registry.*;
import org.sindercube.wordstones.util.ExtraPlayerEvents;
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
		WordstonesTags.init();

		WordstonesItemGroups.init();
		WordstonesSoundEvents.init();
		WordstonesParticleTypes.init();

		WordstonesCommands.init();
		WordstonesPackets.init();

		LootTableEvents.MODIFY.register(WordstonesLootTableChanges::modifyLootTables);
		ExtraPlayerEvents.BEFORE_DEATH.register(LastWillItem::beforeDeath);
		UseEntityCallback.EVENT.register(EnchantedSquidEntity::tryTransformSquid);

		ResourceManagerHelperImpl.registerBuiltinResourcePack(
			of("forgiving-wordstones"),
			"datapack/forgiving_wordstones",
			FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(),
			Text.translatable("datapack.wordstones.forgiving_wordstones"),
			ResourcePackActivationType.NORMAL
		);
	}

}
