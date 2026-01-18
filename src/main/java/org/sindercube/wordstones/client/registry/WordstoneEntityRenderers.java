package org.sindercube.wordstones.client.registry;

import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.client.content.renderer.block.SteleRenderer;
import org.sindercube.wordstones.client.content.renderer.block.WordstoneRenderer;
import org.sindercube.wordstones.client.content.renderer.entity.EnchantedSquidRenderer;
import org.sindercube.wordstones.registry.WordstonesBlockEntityTypes;
import org.sindercube.wordstones.registry.WordstonesEntityTypes;

public class WordstoneEntityRenderers {

	public static final EntityModelLayer ENCHANTED_SQUID_LAYER = new EntityModelLayer(Wordstones.of("enchanted_squid"), "main");

	public static void init() {
		EntityModelLayerRegistry.registerModelLayer(ENCHANTED_SQUID_LAYER, SquidEntityModel::getTexturedModelData);
		EntityRendererRegistry.register(WordstonesEntityTypes.ENCHANTED_SQUID, EnchantedSquidRenderer::getRenderer);
		BlockEntityRendererFactories.register(WordstonesBlockEntityTypes.WORDSTONE, WordstoneRenderer::new);
		BlockEntityRendererFactories.register(WordstonesBlockEntityTypes.STELE, SteleRenderer::new);
	}

}
