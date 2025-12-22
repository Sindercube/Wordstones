package org.sindercube.wordstones.registry;

import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import org.sindercube.wordstones.Wordstones;

public class WordstoneTags {

	public static void init() {}

	public static final TagKey<Item> KEPT_ACROSS_TELEPORTATION = of("kept_across_teleportation", RegistryKeys.ITEM);

	public static <T> TagKey<T> of(String name, RegistryKey<Registry<T>> registry) {
		return TagKey.of(registry, Wordstones.of(name));
	}

}
