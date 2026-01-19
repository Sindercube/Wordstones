package org.sindercube.wordstones;

import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.item.Item;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import org.sindercube.wordstones.registry.WordstonesItems;

public class WordstonesLootTableChanges {

	public static void modifyLootTables(RegistryKey<LootTable> key, LootTable.Builder builder, LootTableSource source, RegistryWrapper.WrapperLookup wrapper) {
		switch (key.getValue().toString()) {
			case "minecraft:chests/stronghold_library" -> addPool(builder, WordstonesItems.ENCHANTED_QUILL, 1);
			case "minecraft:chests/stronghold_crossing" -> addPool(builder, WordstonesItems.ENCHANTED_QUILL, 0.66F);
			case "minecraft:chests/end_city_treasure" -> addPool(builder, WordstonesItems.ENCHANTED_QUILL, 0.5F);
			case "minecraft:chests/stronghold_corridor" -> addPool(builder, WordstonesItems.ENCHANTED_QUILL, 0.33F);
		}
	}

	public static void addPool(LootTable.Builder builder, Item item, float chance) {
		builder.pool(LootPool.builder()
			.with(ItemEntry.builder(item))
			.rolls(ConstantLootNumberProvider.create(1))
			.conditionally(RandomChanceLootCondition.builder(chance).build())
			.build()
		);
	}

}
