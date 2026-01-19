package org.sindercube.wordstones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.client.ModelPredicateProviderRegistry;
import org.sindercube.wordstones.content.item.EnchantedQuillItem;
import org.sindercube.wordstones.content.item.LastWillItem;
import org.sindercube.wordstones.content.item.LocationBindingItem;
import org.sindercube.wordstones.content.item.TomeItem;

import java.util.function.Function;

public class WordstonesItems {

	public static void init() {}

	@Environment(EnvType.CLIENT)
	public static void clientInit() {
		ModelPredicateProviderRegistry.register(
			LocationBindingItem.class,
			Wordstones.of("bound_location"),
			LocationBindingItem::getLocationPredicate
		);
	}

	public static final Item EMPTY_QUILL = register("empty_quill",
		new Item.Settings()
			.maxCount(1)
	);

	public static final Item ENCHANTED_QUILL = register("enchanted_quill",
		EnchantedQuillItem::new,
		new Item.Settings()
			.maxCount(1)
			.rarity(Rarity.RARE)
			.maxDamage(8)
	);

	public static final Item TOME = register("tome",
		TomeItem::new,
		new Item.Settings()
	);

	public static final Item LAST_WILL = register("last_will",
		LastWillItem::new,
		new Item.Settings()
			.maxCount(1)
			.rarity(Rarity.RARE)
	);

	public static final Item ENCHANTED_SQUID_SPAWN_EGG = register("enchanted_squid_spawn_egg",
		settings -> new SpawnEggItem(WordstonesEntityTypes.ENCHANTED_SQUID, 0xA5A5FF, 0x7132A1, settings)
	);

	public static Item register(String name) {
		return register(name, Item::new);
	}

	public static Item register(String name, Function<Item.Settings, Item> function) {
		return register(name, function, new Item.Settings());
	}

	public static Item register(String name, Item.Settings settings) {
		return register(name, Item::new, settings);
	}

	public static Item register(String name, Function<Item.Settings, Item> function, Item.Settings settings) {
		Identifier id = Wordstones.of(name);
		RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
		Item item = function.apply(settings);
		return Registry.register(Registries.ITEM, key, item);
	}

}
