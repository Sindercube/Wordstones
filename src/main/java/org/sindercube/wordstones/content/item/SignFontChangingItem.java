package org.sindercube.wordstones.content.item;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SignChangingItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public interface SignFontChangingItem extends SignChangingItem {

	Identifier DEFAULT_FONT = Identifier.of("default");

	Identifier getFont();
	SoundEvent getSoundEvent();

	@Override
	default boolean useOnSign(World world, SignBlockEntity entity, boolean front, PlayerEntity player) {
		boolean changed = entity.changeText(this::changeFont, front);
		if (changed) {
			world.playSound(null, entity.getPos(), this.getSoundEvent(), SoundCategory.BLOCKS, 1, 1);
		}
		return changed;
	}

	default SignText changeFont(SignText text) {
		for (int i = 0; i < text.getMessages(true).length; i++) {
			Text message = text.getMessage(i, true);
			if (message.getString().isEmpty()) continue;

			Identifier font = !message.getStyle().getFont().equals(this.getFont()) ? this.getFont() : DEFAULT_FONT;
			text = text.withMessage(i, message.copy().styled(style -> style.withFont(font)));
		}
		return text;
	}

}
