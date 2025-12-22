package org.sindercube.wordstones.registry;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

public class WordstoneItemGroups {

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries ->
			entries.addAfter(Items.TOTEM_OF_UNDYING, WordstoneItems.LAST_WILL)
		);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries ->
			entries.addAfter(Items.ENCHANTING_TABLE, WordstoneBlocks.WORDSTONE)
		);
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries ->
			entries.addAfter(Items.ENDER_CHEST, WordstoneBlocks.DROP_BOX)
		);
	}

}
