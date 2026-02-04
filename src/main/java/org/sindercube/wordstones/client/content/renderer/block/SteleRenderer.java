package org.sindercube.wordstones.client.content.renderer.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignText;
import net.minecraft.block.enums.SlabType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.sindercube.wordstones.content.block.SteleBlock;
import org.sindercube.wordstones.content.block.entity.SteleEntity;

public class SteleRenderer implements BlockEntityRenderer<SteleEntity> {

	public static final double RENDER_DISTANCE = MathHelper.square(16);
	public static final float TEXT_OFFSET = 0.125F + 0.001F;
	public static final float TEXT_SCALE = 0.0333F;
	public static final float ATTACHED_OFFSET = 0.375F;

	protected final TextRenderer textRenderer;
	protected final BlockRenderManager manager;

	public SteleRenderer(BlockEntityRendererFactory.Context context) {
		this.textRenderer = context.getTextRenderer();
		this.manager = context.getRenderManager();
	}

	@Override
	public void render(SteleEntity entity, float f, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
		World world = entity.getWorld();
		if (world == null) return;

		BlockState state = entity.getCachedState();
		if (SteleBlock.hasTop(state)) {
			this.renderHalf(world, state, entity, SlabType.TOP, 0, matrices, vertexConsumers, light);
		}
		if (SteleBlock.hasBottom(state)) {
			this.renderHalf(world, state, entity, SlabType.BOTTOM, 1, matrices, vertexConsumers, light);
		}
	}

	public void renderHalf(World world, BlockState state, SteleEntity entity, SlabType half, int line, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		matrices.push();

		state = state.with(SteleBlock.TYPE, half);
		float rotation = SteleBlock.getRenderRotationDegrees(state);
		this.renderBlock(world, entity, state, matrices, vertexConsumers);

		matrices.translate(0.5F, 0.875F, 0.5F);
		if (state.get(SteleBlock.ATTACHED)) switch (SteleBlock.getRotation(state)) {
			case NORTH -> matrices.translate(0, 0, ATTACHED_OFFSET);
			case EAST -> matrices.translate(-ATTACHED_OFFSET, 0, 0);
			case SOUTH -> matrices.translate(0, 0, -ATTACHED_OFFSET);
			case WEST -> matrices.translate(ATTACHED_OFFSET, 0, 0);
		}

		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation));
		this.renderText(entity.getPos(), entity.getFrontText(), line, matrices, vertexConsumers, light, entity.getTextLineHeight());

		matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-180));
		this.renderText(entity.getPos(), entity.getBackText(), line, matrices, vertexConsumers, light, entity.getTextLineHeight());
		matrices.pop();
	}

	public void renderBlock(World world, SteleEntity entity, BlockState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
		this.manager.renderBlock(
			state,
			entity.getPos(), world, matrices,
			vertexConsumers.getBuffer(RenderLayers.getBlockLayer(state)),
			false,
			world.getRandom()
		);
	}

	public void renderText(BlockPos pos, SignText signText, int line, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int lineHeight) {
		Text text = signText.getMessage(line, MinecraftClient.getInstance().shouldFilterText());
		if (text == null) return;

		int color = getColor(signText);
		int glowColor = color;
		boolean drawOutline = false;

		if (signText.isGlowing()) {
			color = signText.getColor().getSignColor();
			drawOutline = shouldRender(pos, color);
		}

		float x = (float)(-this.textRenderer.getWidth(text) / 2) + 0.25F;
		float y = line * lineHeight - line;

		matrices.push();

		matrices.translate(0, 0, TEXT_OFFSET);
		matrices.scale(TEXT_SCALE, -TEXT_SCALE, -TEXT_SCALE);

		if (drawOutline) {
			this.textRenderer.drawWithOutline(text.asOrderedText(), x, y, color, glowColor, matrices.peek().getPositionMatrix(), vertexConsumers, 15728880);
		} else {
			this.textRenderer.draw(text, x, y, color, false, matrices.peek().getPositionMatrix(), vertexConsumers, TextRenderer.TextLayerType.POLYGON_OFFSET, 0, light);
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
