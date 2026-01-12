package org.sindercube.wordstones.client.registry;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import org.sindercube.wordstones.client.content.renderer.block.SteleRenderer;
import org.sindercube.wordstones.client.content.renderer.block.WordstoneRenderer;
import org.sindercube.wordstones.registry.WordstoneBlockEntityTypes;

public class WordstoneEntityRenderers {

	public static void init() {
		BlockEntityRendererFactories.register(WordstoneBlockEntityTypes.WORDSTONE, WordstoneRenderer::new);
		BlockEntityRendererFactories.register(WordstoneBlockEntityTypes.STELE, SteleRenderer::new);
	}

}
