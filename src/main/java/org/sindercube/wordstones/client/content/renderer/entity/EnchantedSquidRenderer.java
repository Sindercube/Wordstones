package org.sindercube.wordstones.client.content.renderer.entity;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.SquidEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.SquidEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.content.entity.EnchantedSquidEntity;

public class EnchantedSquidRenderer extends SquidEntityRenderer<EnchantedSquidEntity> {

	private static final Identifier TEXTURE = Wordstones.of("textures/entity/squid/enchanted.png");
	public static final EntityModelLayer LAYER = new EntityModelLayer(Wordstones.of("enchanted_squid"), "main");

	public EnchantedSquidRenderer(EntityRendererFactory.Context context, SquidEntityModel<EnchantedSquidEntity> model) {
		super(context, model);
	}

	public static EnchantedSquidRenderer getRenderer(EntityRendererFactory.Context context) {
		return new EnchantedSquidRenderer(context, new SquidEntityModel<>(context.getPart(LAYER)));
	}

	@Override
	public Identifier getTexture(EnchantedSquidEntity entity) {
		return TEXTURE;
	}

	@Override
	protected int getBlockLight(EnchantedSquidEntity glowSquidEntity, BlockPos blockPos) {
		return 3;
	}

}
