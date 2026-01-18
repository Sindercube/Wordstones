package org.sindercube.wordstones.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

public class WordstonesItemGroups {

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
			entries.addAfter(Items.ENDER_EYE, WordstonesItems.EMPTY_QUILL);
			entries.addAfter(Items.ENDER_EYE, WordstonesItems.ENCHANTED_QUILL);
			entries.addAfter(Items.ENDER_EYE, WordstonesItems.TOME);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
			entries.addAfter(Items.TOTEM_OF_UNDYING, WordstonesItems.LAST_WILL);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
			entries.addAfter(Items.BOOK, WordstonesItems.TOME);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
			entries.addAfter(Blocks.ENCHANTING_TABLE, WordstonesBlocks.WORDSTONE);
			entries.addAfter(Blocks.ENDER_CHEST, WordstonesBlocks.DROP_BOX);
			entries.addBefore(Blocks.CHEST, WordstonesBlocks.STONE_STELE, WordstonesBlocks.DEEPSLATE_STELE);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register(entries -> {
			entries.addAfter(Items.ELDER_GUARDIAN_SPAWN_EGG, WordstonesItems.ENCHANTED_SQUID_SPAWN_EGG);
		});
	}

}
