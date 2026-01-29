package org.sindercube.wordstones.client.content.renderer.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.OrderedText;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.*;
import org.sindercube.wordstones.content.block.SteleBlock;
import org.sindercube.wordstones.content.block.entity.SteleEntity;

import java.util.List;

public class SteleRenderer implements BlockEntityRenderer<SteleEntity> {

	private static final double RENDER_DISTANCE = MathHelper.square(16);
	private static final float TEXT_OFFSET = 0.125F + 0.001F;
	private static final float TEXT_SCALE = 0.0333F;

	private final TextRenderer textRenderer;

	public SteleRenderer(BlockEntityRendererFactory.Context context) {
		this.textRenderer = context.getTextRenderer();
	}

	@Override
	public void render(SteleEntity entity, float f, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		matrices.push();
		BlockState state = entity.getCachedState();
		SteleBlock block = (SteleBlock) state.getBlock();

		matrices.translate(0.5F, 0.875F, 0.5F);

		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-block.getRotationDegrees(state)));
		this.renderText(entity.getPos(), entity.getFrontText(), matrices, vertexConsumers, light, entity.getTextLineHeight(), entity.getMaxTextWidth());

		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));
		this.renderText(entity.getPos(), entity.getBackText(), matrices, vertexConsumers, light, entity.getTextLineHeight(), entity.getMaxTextWidth());
		matrices.pop();
	}

	void renderText(BlockPos pos, SignText signText, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int lineHeight, int lineWidth) {
		matrices.push();

		matrices.translate(0, 0, TEXT_OFFSET);
		matrices.scale(TEXT_SCALE, -TEXT_SCALE, -TEXT_SCALE);

		OrderedText[] orderedTexts = signText.getOrderedMessages(MinecraftClient.getInstance().shouldFilterText(), (text) -> {
			List<OrderedText> list = this.textRenderer.wrapLines(text, lineWidth);
			return list.isEmpty() ? OrderedText.EMPTY : list.getFirst();
		});

		int color = getColor(signText);
		int glowColor = color;
		boolean drawOutline = false;

		if (signText.isGlowing()) {
			color = signText.getColor().getSignColor();
			drawOutline = shouldRender(pos, color);
		}

		for (int i = 0; i < 2; ++i) {
			OrderedText orderedText = orderedTexts[i];
			float x = (float)(-this.textRenderer.getWidth(orderedText) / 2);
			float y = i * lineHeight - i;
			if (drawOutline) {
				this.textRenderer.drawWithOutline(orderedText, x, y, color, glowColor, matrices.peek().getPositionMatrix(), vertexConsumers, 15728880);
			} else {
				this.textRenderer.draw(orderedText, x, y, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
			}
		}

		matrices.pop();
	}

	public static boolean shouldRender(BlockPos pos, int signColor) {
		if (signColor == DyeColor.BLACK.getSignColor()) return true;

		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player != null && client.options.getPerspective().isFirstPerson() && player.isUsingSpyglass()) {
			return true;
		}

		Entity camera = client.getCameraEntity();
		return camera != null && camera.squaredDistanceTo(Vec3d.ofCenter(pos)) < RENDER_DISTANCE;
	}

	public static int getColor(SignText sign) {
		int color = sign.getColor().getSignColor();
		if (color == DyeColor.BLACK.getSignColor() && sign.isGlowing()) {
			return -0xF1434;
		}
		int r = (int)((double) ColorHelper.Argb.getRed(color) * 0.4F);
		int g = (int)((double) ColorHelper.Argb.getGreen(color) * 0.4F);
		int b = (int)((double) ColorHelper.Argb.getBlue(color) * 0.4F);
		return ColorHelper.Argb.getArgb(0, r, g, b);
	}

}
