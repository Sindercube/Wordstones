package org.sindercube.wordstones.client.content.renderer.block;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.World;
import org.sindercube.wordstones.content.block.WordstoneBlock;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;

public class WordstoneRenderer implements BlockEntityRenderer<WordstoneEntity> {

	protected final BlockRenderManager manager;
	protected final BookModel book;

	float pageTurningSpeed = 0;
	float nextPageTurningSpeed = 0;
	float lastBookRotation = 0;
	float bookRotation = 0;
	float targetBookRotation = 0;
	float flipRandom = 0;
	float pageAngle;
	float nextPageAngle;
	float flipTurn = 0;

	public WordstoneRenderer(BlockEntityRendererFactory.Context context) {
		this.manager = context.getRenderManager();
		this.book = new BookModel(context.getLayerModelPart(EntityModelLayers.BOOK));
	}

	@Override
	public void render(WordstoneEntity wordstone, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
//		this.pageTurningSpeed = this.nextPageTurningSpeed;

		this.lastBookRotation = this.bookRotation;

		World world = wordstone.getWorld();
		if (world == null) return;

		BlockState state = wordstone.getWorld().getBlockState(wordstone.getPos());
		if (state.isAir()) return;

		PlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null) return;

		float distance = this.distanceTo(player, wordstone);

		if (distance < 5) {
			double x = player.getX() - (wordstone.getPos().getX() + 0.5D);
			double z = player.getZ() - (wordstone.getPos().getZ() + 0.5D);
			this.targetBookRotation = (float)MathHelper.atan2(z, x);
			this.pageTurningSpeed += 0.1F;
			if (this.pageTurningSpeed < 0.5F || player.getRandom().nextInt(40) == 0) {
				float flip = this.flipRandom;

				do {
					this.flipRandom += (float) (player.getRandom().nextInt(4) - player.getRandom().nextInt(4));
				} while(flip == this.flipRandom);
			}
		} else {
			this.targetBookRotation += 0.02F;
			this.pageTurningSpeed -= 0.1F;
		}
		this.pageTurningSpeed = MathHelper.clamp(this.pageTurningSpeed, 0.0F, 1.0F);

//		while (this.bookRotation >= Math.PI) {
//			this.bookRotation -= (float) Math.PI * 2F;
//		}
//
//		while (this.bookRotation < -Math.PI) {
//			this.bookRotation += (float) Math.PI * 2F;
//		}
//
//		while (this.targetBookRotation >= Math.PI) {
//			this.targetBookRotation -= (float) Math.PI * 2F;
//		}
//
//		while (this.targetBookRotation < -Math.PI) {
//			this.targetBookRotation += (float) Math.PI * 2F;
//		}

//		float g;
//		for(g = this.targetBookRotation - this.bookRotation; g >= (float)Math.PI; g -= ((float)Math.PI * 2F)) {
//		}

		float bookRotation = this.targetBookRotation - this.bookRotation;
		bookRotation -= (float) Math.PI * 2;

		while (bookRotation < -Math.PI) {
			bookRotation += (float) Math.PI * 2F;
		}

		this.bookRotation += bookRotation * 0.4F;

		this.pageAngle = this.nextPageAngle;
		float angle = (this.flipRandom - this.nextPageAngle) * 0.4F;
		angle = MathHelper.clamp(angle, -0.2F, 0.2F);
		this.flipTurn += (angle - this.flipTurn) * 0.9F;
		this.nextPageAngle += this.flipTurn;

		matrices.push();
		matrices.translate(0.5F, 1, 0.5F);

		Direction direction = state.get(WordstoneBlock.FACING);
		matrices.translate(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());

		float tick = wordstone.ticks + tickDelta;
		matrices.translate(0.0F, 0.1F + MathHelper.sin(tick * 0.1F) * 0.01F, 0.0F);

		float rotation = this.bookRotation - this.lastBookRotation;
//		rotation -= (float)Math.PI * 2F;

		while (rotation < -Math.PI) {
			rotation += (float) Math.PI * 2F;
		}

		float rotationDelta = this.lastBookRotation + rotation * tickDelta;
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-rotationDelta));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45));
		float pageAngle = MathHelper.lerp(tickDelta, this.pageAngle, this.nextPageAngle);
		float leftAngle = MathHelper.fractionalPart(pageAngle + 0.25F) * 1.6F - 0.3F;
		float rightAngle = MathHelper.fractionalPart(pageAngle + 0.75F) * 1.6F - 0.3F;
		float turnSpeed = MathHelper.lerp(tickDelta, this.pageTurningSpeed, this.pageTurningSpeed);
		this.book.setPageAngles(tick, MathHelper.clamp(leftAngle, 0.0F, 1.0F), MathHelper.clamp(rightAngle, 0.0F, 1.0F), turnSpeed);
		VertexConsumer vertexConsumer = EnchantingTableBlockEntityRenderer.BOOK_TEXTURE.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid);
		this.book.renderBook(matrices, vertexConsumer, light, overlay, -1);
		matrices.pop();




//		matrices.push();
//		matrices.translate(1.5F, 0.75F, 0.5F);
//		float tick = wordstone.ticks + tickDelta;
//		matrices.translate(0.0F, 0.1F + MathHelper.sin(tick * 0.1F) * 0.01F, 0.0F);
//
//		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(80.0F));
////		float l = MathHelper.lerp(tickDelta, enchantingTableBlockEntity.pageAngle, enchantingTableBlockEntity.nextPageAngle);
////		float m = MathHelper.fractionalPart(l + 0.25F) * 1.6F - 0.3F;
////		float n = MathHelper.fractionalPart(l + 0.75F) * 1.6F - 0.3F;
////		float o = MathHelper.lerp(tickDelta, enchantingTableBlockEntity.pageTurningSpeed, enchantingTableBlockEntity.nextPageTurningSpeed);
////		this.book.setPageAngles(g, MathHelper.clamp(m, 0.0F, 1.0F), MathHelper.clamp(n, 0.0F, 1.0F), o);
//
//
////		World world = entity.getWorld();
////		PlayerEntity playerEntity = entity.getWorld().getClosestPlayer((double)pos.getX() + (double)0.5F, (double)pos.getY() + (double)0.5F, (double)pos.getZ() + (double)0.5F, (double)3.0F, false);
//		PlayerEntity player = MinecraftClient.getInstance().player;
//		float distance = this.distanceTo(player, wordstone);
//
//		double d = player.getX() - ((double)wordstone.getPos().getX() + (double)0.5F);
//		double e = player.getZ() - ((double)wordstone.getPos().getZ() + (double)0.5F);
//		float rotation = (float) MathHelper.atan2(e, d) * tickDelta;
//		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-rotation));
//
//		float turnSpeed = distance > 5 ? 0 : 1;
//		this.book.setPageAngles(tick, 0.90F, 0.95F, turnSpeed);
//		VertexConsumer vertexConsumer = EnchantingTableBlockEntityRenderer.BOOK_TEXTURE.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid);
//		this.book.renderBook(matrices, vertexConsumer, light, overlay, -1);
//		matrices.pop();
	}

	public float distanceTo(PlayerEntity player, WordstoneEntity wordstone) {
		float x = (float) player.getX() - wordstone.getPos().getX();
		float y = (float) player.getY() - wordstone.getPos().getY();
		float z = (float) player.getZ() - wordstone.getPos().getZ();
		return MathHelper.sqrt(x * x + y * y + z * z);
	}

}
