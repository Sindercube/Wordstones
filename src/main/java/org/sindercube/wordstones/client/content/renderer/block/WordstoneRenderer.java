package org.sindercube.wordstones.client.content.renderer.block;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.sindercube.wordstones.content.block.WordstoneBlock;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;

public class WordstoneRenderer implements BlockEntityRenderer<WordstoneEntity> {

	protected final BlockRenderManager manager;
	protected final BookModel book;

	public WordstoneRenderer(BlockEntityRendererFactory.Context context) {
		this.manager = context.getRenderManager();
		this.book = new BookModel(context.getLayerModelPart(EntityModelLayers.BOOK));
	}

	@Override
	public void render(WordstoneEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		World world = entity.getWorld();
		if (world == null) return;

		BlockState state = world.getBlockState(entity.getPos());
		if (state.isAir()) return;

		matrices.push();
		matrices.translate(0.5F, 0.8F, 0.5F);

		Direction direction = state.get(WordstoneBlock.FACING);
		matrices.translate(direction.getOffsetX() * 0.8F, direction.getOffsetY(), direction.getOffsetZ() * 0.8F);

		WordstoneEntity.RenderState renderState = entity.renderState;

		float pageTurnAmount = (float)renderState.ticks + tickDelta;
		matrices.translate(0, 0.1F + MathHelper.sin(pageTurnAmount * 0.1F) * 0.01F, 0);

		float rotation = renderState.bookRotation - renderState.lastBookRotation;
		if (rotation >= (float) Math.PI) {
			rotation -= (float) Math.PI * 2;
		}
		while (rotation < -(float) Math.PI) {
			rotation += (float) Math.PI * 2;
		}

		float rotationDelta = renderState.lastBookRotation + rotation * tickDelta;
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-rotationDelta));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(80));
		float flipAmount = MathHelper.lerp(tickDelta, renderState.pageAngle, renderState.nextPageAngle);
		float leftFlipAmount = MathHelper.fractionalPart(flipAmount + 0.25F) * 1.6F - 0.3F;
		leftFlipAmount = MathHelper.clamp(leftFlipAmount, 0, 1);
		float rightFlipAmount = MathHelper.fractionalPart(flipAmount + 0.75F) * 1.6F - 0.3F;
		rightFlipAmount = MathHelper.clamp(rightFlipAmount, 0, 1);
		float pageTurnSpeed = MathHelper.lerp(tickDelta, renderState.pageTurningSpeed, renderState.nextPageTurningSpeed);
		this.book.setPageAngles(pageTurnAmount, leftFlipAmount, rightFlipAmount, pageTurnSpeed);
		VertexConsumer vertexConsumer = EnchantingTableBlockEntityRenderer.BOOK_TEXTURE.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid);
		this.book.renderBook(matrices, vertexConsumer, light, overlay, -1);
		matrices.pop();
	}

}
