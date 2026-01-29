package org.sindercube.wordstones.client.content.screen;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractSignEditScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;
import org.sindercube.wordstones.content.block.SteleBlock;
import org.sindercube.wordstones.content.block.entity.SteleEntity;

public class SteleEditScreen extends AbstractSignEditScreen {

	protected final Identifier texture;
	protected final boolean topLine;
	protected final boolean bottomLine;
	protected final int lines;

	public SteleEditScreen(SteleEntity entity, boolean front, boolean filtered) {
		super(entity, front, filtered, Text.translatable("screen.stele.edit"));
		this.texture = Identifier.tryParse(this.signType.name())
			.withPath(path -> "textures/gui/screen/stele/" + path)
			.withPath(path -> path + "_" + entity.getCachedState().get(SteleBlock.TYPE) + ".png");
		this.topLine = entity.hasTopLine();
		this.bottomLine = entity.hasBottomLine();
		this.lines = this.topLine && this.bottomLine ? 2 : 1;
	}

	@Override
	protected void init() {
		super.init();
		if (this.bottomLine && !this.topLine) this.currentRow = 1;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//		int min = this.topLine ? 0 : 1;
//		int max = this.bottomLine ? 1 : 0;

		int min = this.bottomLine && !this.topLine ? 1 : 0;
		int max = !this.bottomLine && this.topLine ? 0 : 1;

		if (keyCode == 265) {
			this.currentRow = Math.clamp(this.currentRow - 1, min, max);
			this.selectionManager.putCursorAtEnd();
			return true;
		}
		if (keyCode != 264 && keyCode != 257 && keyCode != 335) {
			return this.selectionManager.handleSpecialKey(keyCode) || super.keyPressed(keyCode, scanCode, modifiers);
		}
		this.currentRow = Math.clamp(this.currentRow + 1, min, max);
		this.selectionManager.putCursorAtEnd();
		return true;
	}

	@Override
	public void removed() {
		ClientPlayNetworkHandler handler = this.client.getNetworkHandler();
		if (handler != null) {
			handler.sendPacket(new UpdateSignC2SPacket(this.blockEntity.getPos(), this.front, this.messages[0], this.messages[1], "", ""));
		}
	}

	protected void translateForRender(DrawContext context, BlockState state) {
		context.getMatrices().translate((float) this.width / 2, 120, 50);
	}

	@Override
	public void renderSignText(DrawContext context) {
		context.getMatrices().translate(0, 61.5F, 0);
		super.renderSignText(context);
	}

	@Override
	protected void renderSignBackground(DrawContext context, BlockState state) {
		context.getMatrices().scale(6, 6, 1);
		context.drawTexture(this.texture, -8, -8, 0, 0, 16, 16, 16, 16);
	}

	@Override
	protected Vector3f getTextScale() {
		return new Vector3f(3, 3, 3);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		int length = this.messages[this.currentRow].length();
		if (length >= 4) return false;

		if (Character.isLetter(chr)) chr = Character.toUpperCase(chr);
		this.selectionManager.insert(chr);
		return true;
	}

}
