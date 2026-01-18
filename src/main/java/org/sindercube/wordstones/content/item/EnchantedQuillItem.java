package org.sindercube.wordstones.content.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.sindercube.wordstones.registry.WordstonesItems;
import org.sindercube.wordstones.registry.WordstonesSoundEvents;

public class EnchantedQuillItem extends Item implements SignFontChangingItem {

	public EnchantedQuillItem(Settings settings) {
		super(settings);
	}

	@Override
	public Identifier getFont() {
		return Identifier.of("alt");
	}

	@Override
	public SoundEvent getSoundEvent() {
		return WordstonesSoundEvents.ITEM_ENCHANTED_QUILL_USE_SIGN;
	}

	@Override
	public ItemStack getRecipeRemainder(ItemStack stack) {
		ItemStack copy = stack.copy();
		copy.setDamage(stack.getDamage() + 1);

		if (copy.getDamage() < copy.getMaxDamage()) return copy;
		return WordstonesItems.EMPTY_QUILL.getDefaultStack();
	}

	@Override
	public boolean canRepair(ItemStack stack, ItemStack ingredient) {
		return false;
	}

}
