package org.sindercube.wordstones.registry;

import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import org.sindercube.wordstones.Wordstones;

public class WordstonesTags {

	public static void init() {}

	public static final TagKey<Item> KEPT_ACROSS_TELEPORTATION = of("kept_across_teleportation", RegistryKeys.ITEM);
	public static final TagKey<EntityType<?>> ENCHANTABLE_SQUID = of("enchantable_squid", RegistryKeys.ENTITY_TYPE);

	public static <T> TagKey<T> of(String name, RegistryKey<Registry<T>> registry) {
		return TagKey.of(registry, Wordstones.of(name));
	}

}
