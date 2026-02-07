package org.sindercube.wordstones.content.block.entity;

import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;

public class DropBoxInventory extends PlayerInventory {

	public DropBoxInventory() {
		super(null);
	}

	public static DropBoxInventory copyFromPlayer(PlayerEntity player, @Nullable TagKey<Item> excluded) {
		DropBoxInventory inventory = new DropBoxInventory();
		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = player.getInventory().getStack(slot);
			if (excluded != null && stack.isIn(excluded)) {
				continue;
			}
			if (EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
				continue;
			}
			if (EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
				continue;
			}
			inventory.setStack(slot, stack);
		}
		return inventory;
	}

	public NbtList writeNbt(NbtList list, RegistryWrapper.WrapperLookup lookup) {
		for (int i = 0; i < this.main.size(); ++i) {
			ItemStack stack = this.main.get(i);
			if (stack.isEmpty()) continue;

			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.putByte("Slot", (byte) i);
			list.add(stack.encode(lookup, nbtCompound));
		}

		for (int i = 0; i < this.armor.size(); ++i) {
			ItemStack stack = this.armor.get(i);
			if (stack.isEmpty()) continue;

			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.putByte("Slot", (byte) (i + 100));
			list.add(stack.encode(lookup, nbtCompound));
		}

		for (int i = 0; i < this.offHand.size(); ++i) {
			ItemStack stack = this.offHand.get(i);
			if (stack.isEmpty()) continue;

			NbtCompound nbtCompound = new NbtCompound();
			nbtCompound.putByte("Slot", (byte) (i + 150));
			list.add(stack.encode(lookup, nbtCompound));
		}

		return list;
	}

	public void readNbt(NbtList list, RegistryWrapper.WrapperLookup lookup) {
		this.main.clear();
		this.armor.clear();
		this.offHand.clear();

		for (int i = 0; i < list.size(); ++i) {
			NbtCompound compound = list.getCompound(i);
			int slot = compound.getByte("Slot") & 255;
			ItemStack stack = ItemStack.fromNbt(lookup, compound).orElse(ItemStack.EMPTY);
			if (slot < this.main.size()) {
				this.main.set(slot, stack);
			} else if (slot >= 100 && slot < this.armor.size() + 100) {
				this.armor.set(slot - 100, stack);
			} else if (slot >= 150 && slot < this.offHand.size() + 150) {
				this.offHand.set(slot - 150, stack);
			}
		}
	}

}
