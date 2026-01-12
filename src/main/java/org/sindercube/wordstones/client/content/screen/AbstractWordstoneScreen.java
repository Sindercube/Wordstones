package org.sindercube.wordstones.client.content.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.EnchantingTableBlockEntityRenderer;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.sindercube.wordstones.client.WordstoneTypingEvent;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;

public abstract class AbstractWordstoneScreen extends Screen {

	protected final WordstoneEntity wordstone;
	protected String word;

	@Nullable protected ButtonWidget doneButton;
	@Nullable protected BookModel bookModel;

	public AbstractWordstoneScreen(Text title, WordstoneEntity wordstone) {
		super(title);
		this.wordstone = wordstone;
		this.word = wordstone.getWord() != null ? wordstone.getWord().value() : "";
	}

	abstract public void onDone();

	protected void onDone(ButtonWidget button) {
		this.onDone();
	}

	@Override
	protected void init() {
		super.init();

		this.doneButton = ButtonWidget.builder(ScreenTexts.DONE, this::onDone)
			.dimensions(this.width / 2 - 100, this.height / 4 + 144, 200, 20)
			.build();
		this.addDrawableChild(this.doneButton);
		this.update();

		MinecraftClient client = this.client;
		if (client != null) this.bookModel = new BookModel(client.getEntityModelLoader().getModelPart(EntityModelLayers.BOOK));
	}

	protected boolean canUse() {
		return this.client != null && this.client.player != null && !this.wordstone.isRemoved() && !this.wordstone.isPlayerTooFar(this.client.player);
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.canUse()) this.close();
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		if (this.bookModel == null) return;

		DiffuseLighting.disableGuiDepthLighting();
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 40, 0xFFFFFF);

		context.getMatrices().push();
		this.translateForRender(context);
		context.getMatrices().push();
		this.renderBook(context);
		context.getMatrices().pop();
		this.renderText(context);
		context.getMatrices().pop();

		DiffuseLighting.enableGuiDepthLighting();
	}

	public void translateForRender(DrawContext context) {
		context.getMatrices().translate((float) this.width / 2, 120, 50);
	}

	public void renderBook(DrawContext context) {
		context.getMatrices().scale(-128, 128, 128);
		context.getMatrices().multiply(RotationAxis.POSITIVE_X.rotationDegrees(45));
		context.getMatrices().multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(90));

		this.bookModel.setPageAngles(0, 0.099F, 0.9F, 1);
		VertexConsumer vertexConsumer = EnchantingTableBlockEntityRenderer.BOOK_TEXTURE.getVertexConsumer(context.getVertexConsumers(), RenderLayer::getEntitySolid);
		this.bookModel.render(context.getMatrices(), vertexConsumer, 0xF000F0, OverlayTexture.DEFAULT_UV);
	}

	public void renderText(DrawContext context) {
		context.getMatrices().translate(-24, -20, 60);
		context.getMatrices().scale(2.5F, 2.5F, 1);

		String word = this.word;
		word += "_".repeat(Math.max(0, 4 - word.length()));

		int spacing = 0;
		for (String letter : word.split("")) {
			int x = spacing - textRenderer.getWidth(letter) / 2;
			context.drawText(this.textRenderer, letter, x, 0, 0x000000, false);
			spacing += 7;
		}
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return switch (keyCode) {
			case GLFW.GLFW_KEY_ESCAPE -> {
				this.close();
				yield true;
			}
			case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
				this.onDone();
				yield true;
			}
			case GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_KEY_DELETE -> {
				if (this.word.isEmpty()) yield true;

				this.word = this.word.substring(0, this.word.length() - 1);
				this.update();
				yield true;
			}
			default -> super.keyPressed(keyCode, scanCode, modifiers);
		};
	}

	@Override
	public boolean charTyped(char letter, int modifiers) {
		if (this.word.length() >= 4) return false;
		if (!Character.isLetter(letter)) return super.charTyped(letter, modifiers);

		letter = Character.toUpperCase(letter);
		this.word += letter;
		this.update();

		MinecraftClient client = MinecraftClient.getInstance();
		if (client != null) WordstoneTypingEvent.WORD_TYPED.invoker().wordTyped(client, this.word);

		return true;
	}

	protected void update() {
		if (this.doneButton != null) this.doneButton.active = this.word.length() == 4;
	}

	@Override
	public void close() {
		if (this.client != null) this.client.setScreen(null);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

}
