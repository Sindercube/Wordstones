package org.sindercube.wordstones.content.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.sindercube.wordstones.registry.WordstonesBlockEntityTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DropBoxEntity extends BlockEntity {

	protected Map<UUID, DropBoxInventory> inventories = new HashMap<>();

	public DropBoxEntity(BlockPos pos, BlockState state) {
        super(WordstonesBlockEntityTypes.DROP_BOX, pos, state);
    }

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.writeNbt(nbt, lookup);
		if (this.inventories == null || this.inventories.isEmpty()) return;

		NbtCompound compound = new NbtCompound();
		this.inventories.forEach((uuid, inventory) -> {
			NbtList list = new NbtList();
			list = inventory.writeNbt(list, lookup);
			compound.put(uuid.toString(), list);
		});
		nbt.put("player_inventories", compound);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
		super.readNbt(nbt, lookup);
		if (!nbt.contains("player_inventories")) return;

		NbtCompound compound = nbt.getCompound("player_inventories");
		compound.getKeys().forEach(uuid -> {
			NbtList list = compound.getList(uuid, 10);
			DropBoxInventory inventory = new DropBoxInventory();
			inventory.readNbt(list, lookup);
			this.inventories.put(UUID.fromString(uuid), inventory);
		});
	}

	public boolean hasInventoryForPlayer(PlayerEntity player) {
		return this.inventories.containsKey(player.getUuid());
	}

	public void putInventoryForPlayer(PlayerEntity player, DropBoxInventory inventory) {
		this.inventories.put(player.getUuid(), inventory);
	}

	@Nullable
	public DropBoxInventory popInventoryForPlayer(PlayerEntity player) {
		return this.inventories.remove(player.getUuid());
	}

	public ActionResult depositItems(PlayerEntity player, @Nullable TagKey<Item> excluded) {
		if (this.hasInventoryForPlayer(player)) return ActionResult.FAIL;

		DropBoxInventory newInventory = DropBoxInventory.copyFromPlayer(player, excluded);
		if (newInventory.isEmpty()) return ActionResult.FAIL;

		this.putInventoryForPlayer(player, newInventory);
		this.unequipPlayer(player, excluded);
		this.markDirty();

		return ActionResult.SUCCESS;
	}

	public ActionResult retrieveItems(PlayerEntity player) {
		if (!this.hasInventoryForPlayer(player)) return ActionResult.FAIL;

		DropBoxInventory inventory = this.popInventoryForPlayer(player);
		if (inventory == null || inventory.isEmpty()) return ActionResult.FAIL;

		this.equipPlayer(inventory, player);
		this.markDirty();

		return ActionResult.SUCCESS;
	}

	public void unequipPlayer(PlayerEntity player, @Nullable TagKey<Item> excluded) {
		for (int slot = 0; slot < player.getInventory().size(); slot++) {
			ItemStack stack = player.getInventory().getStack(slot);

			if (excluded != null && stack.isIn(excluded)) {
				continue;
			}
			if (EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
				continue;
			}

			player.getInventory().setStack(slot, ItemStack.EMPTY);
		}
	}

	public void equipPlayer(DropBoxInventory inventory, PlayerEntity player) {
		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = inventory.getStack(slot);
			if (player.getInventory().getStack(slot).isEmpty()) {
				player.getInventory().setStack(slot, stack);
			} else {
				player.getInventory().offerOrDrop(stack);
			}
		}
		player.currentScreenHandler.sendContentUpdates();
	}

}
