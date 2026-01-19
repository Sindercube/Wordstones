package org.sindercube.wordstones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.client.content.renderer.block.SteleRenderer;
import org.sindercube.wordstones.client.content.renderer.block.WordstoneRenderer;
import org.sindercube.wordstones.client.content.renderer.entity.EnchantedSquidRenderer;
import org.sindercube.wordstones.content.entity.EnchantedSquidEntity;

public class WordstonesEntityTypes {

	public static void init() {
		FabricDefaultAttributeRegistry.register(ENCHANTED_SQUID, EnchantedSquidEntity.createSquidAttributes());
	}

	@Environment(EnvType.CLIENT)
	public static void clientInit() {
		EntityModelLayerRegistry.registerModelLayer(EnchantedSquidRenderer.LAYER, SquidEntityModel::getTexturedModelData);
		EntityRendererRegistry.register(WordstonesEntityTypes.ENCHANTED_SQUID, EnchantedSquidRenderer::getRenderer);
		BlockEntityRendererFactories.register(WordstonesBlockEntityTypes.WORDSTONE, WordstoneRenderer::new);
		BlockEntityRendererFactories.register(WordstonesBlockEntityTypes.STELE, SteleRenderer::new);
	}

	public static final EntityType<EnchantedSquidEntity> ENCHANTED_SQUID = register("enchanted_squid",
		EntityType.Builder.create(EnchantedSquidEntity::new, SpawnGroup.MISC).dimensions(0.8F, 0.8F).eyeHeight(0.4F).maxTrackingRange(8)
	);

	public static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		Identifier id = Wordstones.of(name);
		return Registry.register(Registries.ENTITY_TYPE, id, builder.build());
	}

}
