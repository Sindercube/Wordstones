package org.sindercube.wordstones;

import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.village.TradeOffers;
import org.sindercube.wordstones.registry.WordstonesItems;

import java.util.List;

public class WordstonesLootTableChanges {

	public static void modifyLootTables(RegistryKey<LootTable> key, LootTable.Builder builder, LootTableSource s, RegistryWrapper.WrapperLookup l) {
		switch (key.getValue().toString()) {
			case "minecraft:chests/stronghold_library" -> addPool(builder, WordstonesItems.ENCHANTED_QUILL, 1);
			case "minecraft:chests/stronghold_crossing" -> addPool(builder, WordstonesItems.ENCHANTED_QUILL, 0.666F);
			case "minecraft:chests/end_city_treasure" -> addPool(builder, WordstonesItems.ENCHANTED_QUILL, 0.5F);
			case "minecraft:chests/stronghold_corridor" -> addPool(builder, WordstonesItems.ENCHANTED_QUILL, 0.333F);
		}
	}

	public static void addPool(LootTable.Builder builder, Item item) {
		builder.pool(LootPool.builder()
			.with(ItemEntry.builder(item))
			.rolls(ConstantLootNumberProvider.create(1))
			.build()
		);
	}

	public static void addPool(LootTable.Builder builder, Item item, float chance) {
		builder.pool(LootPool.builder()
			.with(ItemEntry.builder(item))
			.rolls(ConstantLootNumberProvider.create(1))
			.conditionally(RandomChanceLootCondition.builder(chance).build())
			.build()
		);
	}

	public static void modifyPool(LootTable.Builder builder, Item item, int weight) {
		builder.modifyPools(pool ->
			pool.with(ItemEntry.builder(item).weight(weight))
		);
	}

	public static void addOffer(List<TradeOffers.Factory> factories, Item item, int price, int maxUses, int experience) {
		TradeOffers.Factory factory = new TradeOffers.SellItemFactory(new ItemStack(item), price, 1, maxUses, experience);
		factories.add(factory);
	}

}
