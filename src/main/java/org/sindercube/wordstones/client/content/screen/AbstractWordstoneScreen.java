package org.sindercube.wordstones.client.content.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.sindercube.wordstones.client.WordstoneTypingEvent;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;

public abstract class AbstractWordstoneScreen extends Screen {

	public static final Identifier BOOK_TEXTURE = Identifier.ofVanilla("textures/entity/enchanting_table_book.png");

	protected final WordstoneEntity wordstone;
	protected String word;

	@Nullable protected ButtonWidget doneButton;
	@Nullable protected BookModel bookModel;

	public AbstractWordstoneScreen(Text title, WordstoneEntity wordstone) {
		super(title);
		this.wordstone = wordstone;
		this.word = wordstone.getWord() != null ? wordstone.getWord().value() : "";
	}

	@Override
	protected void init() {
		super.init();

		this.doneButton = ButtonWidget.builder(ScreenTexts.DONE, this::onDone)
			.dimensions(this.width / 2 - 100, this.height / 4 + 144, 200, 20)
			.build();
		this.addDrawableChild(this.doneButton);
		this.updateButtons();

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

		int centerX = this.width / 2;
		int centerY = this.height / 2;

		DiffuseLighting.disableGuiDepthLighting();
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 40, 0xFFFFFF);

		MatrixStack matrices = context.getMatrices();
		VertexConsumerProvider vertexConsumers = context.getVertexConsumers();

		this.drawBook(matrices, vertexConsumers, context, centerX, centerY);
		this.drawText(matrices, context, centerX, centerY);

		DiffuseLighting.enableGuiDepthLighting();
	}

	public void drawBook(MatrixStack matrices, VertexConsumerProvider vertexConsumers, DrawContext context, int x, int y) {
		if (this.bookModel == null) return;

		matrices.push();
		matrices.translate((float)x, (float)y, -1024);

		float scale = 128F;
		matrices.scale(-scale, scale, scale);

		matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45));
		matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(90));
		float leftFlipAmount = MathHelper.clamp(MathHelper.fractionalPart(1 + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
		float rightFlipAmount = MathHelper.clamp(MathHelper.fractionalPart(1 + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
		this.bookModel.setPageAngles(0, leftFlipAmount, rightFlipAmount, 1);
		VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.bookModel.getLayer(BOOK_TEXTURE));
		this.bookModel.render(matrices, vertexConsumer, 0xF000F0, OverlayTexture.DEFAULT_UV);
		context.draw();
		matrices.pop();
	}

	public void drawText(MatrixStack matrices, DrawContext context, int centerX, int centerY) {
		matrices.push();
		matrices.translate(-centerX, -centerY, 0);
		matrices.scale(2, 2, 1);

		String word = this.word;
		int missing = Math.max(0, 4 - word.length());
		word += "_".repeat(missing);

		int textX = centerX - 10;
		int textY = centerY - 10;

		int spacing = 0;
		for (String letter : word.split("")) {
			int letterX = textX + spacing - textRenderer.getWidth(letter) / 2;
			context.drawText(this.textRenderer, letter, letterX, textY, 0x000000, false);
			spacing += 7;
		}

		matrices.pop();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean result = switch (keyCode) {
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
				this.updateButtons();
				yield true;
			}
			default -> super.keyPressed(keyCode, scanCode, modifiers);
		};

		return result;
	}

	@Override
	public boolean charTyped(char letter, int modifiers) {
		if (this.word.length() >= 4) return false;
		if (!Character.isLetter(letter)) return super.charTyped(letter, modifiers);

		letter = Character.toUpperCase(letter);
		this.word += letter;
		this.updateButtons();

		MinecraftClient client = MinecraftClient.getInstance();
		if (client != null) WordstoneTypingEvent.WORD_TYPED.invoker().wordTyped(client, this.word);

		return true;
	}

	protected void updateButtons() {
		if (this.doneButton != null)
			this.doneButton.active = this.word.length() == 4;
	}

	abstract public void onDone();

	protected void onDone(ButtonWidget button) {
		this.onDone();
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
