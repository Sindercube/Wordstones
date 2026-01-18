package org.sindercube.wordstones.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.content.entity.EnchantedSquidEntity;

public class WordstonesEntityTypes {

	public static void init() {
		FabricDefaultAttributeRegistry.register(ENCHANTED_SQUID, EnchantedSquidEntity.createSquidAttributes());
	}

	public static final EntityType<EnchantedSquidEntity> ENCHANTED_SQUID = register("enchanted_squid",
		EntityType.Builder.create(EnchantedSquidEntity::new, SpawnGroup.MISC).dimensions(0.8F, 0.8F).eyeHeight(0.4F).maxTrackingRange(8)
	);

	public static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		Identifier id = Wordstones.of(name);
		return Registry.register(Registries.ENTITY_TYPE, id, builder.build());
	}

}
