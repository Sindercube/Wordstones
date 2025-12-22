package org.sindercube.wordstones.content.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import org.sindercube.wordstones.content.entity.WordstonesPlayerInventory;
import org.sindercube.wordstones.registry.WordstoneBlockEntityTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DropBoxEntity extends BlockEntity {

	protected Map<UUID, PlayerInventory> playerInventories = new HashMap<>();

	public DropBoxEntity(BlockPos pos, BlockState state) {
        super(WordstoneBlockEntityTypes.DROP_BOX, pos, state);
    }

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		if (this.playerInventories == null || this.playerInventories.isEmpty()) return;
		NbtCompound compound = new NbtCompound();
		this.playerInventories.forEach((uuid, inventory) -> {
			((WordstonesPlayerInventory)inventory).setRegistryLookup(registryLookup);
			NbtList list = new NbtList();
			list = inventory.writeNbt(list);
			compound.put(uuid.toString(), list);
		});
		nbt.put("player_inventories", compound);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		if (!nbt.contains("player_inventories")) return;

		NbtCompound compound = nbt.getCompound("player_inventories");
		compound.getKeys().forEach(uuid -> {
			NbtList list = compound.getList(uuid, 10);
			PlayerInventory inventory = new PlayerInventory(null);
			((WordstonesPlayerInventory)inventory).setRegistryLookup(registryLookup);
			inventory.readNbt(list);
			this.playerInventories.put(UUID.fromString(uuid), inventory);
		});
	}

	public boolean hasPlayerInventory(PlayerEntity player) {
		return this.playerInventories.containsKey(player.getUuid());
	}

	public ActionResult depositItems(PlayerEntity player) {
		UUID uuid = player.getUuid();
		if (this.playerInventories.containsKey(uuid)) {
			player.sendMessage(Text.translatable("block.wordstones.drop_box.inventory_exists"), true);
			return ActionResult.FAIL;
		}

		PlayerInventory newInventory = copyInventory(player);
		if (newInventory.isEmpty()) return ActionResult.FAIL;

		this.playerInventories.put(uuid, newInventory);
		unequipPlayer(player);

		markDirty();
		return ActionResult.SUCCESS;
	}

	public static PlayerInventory copyInventory(PlayerEntity player) {
		PlayerInventory inventory = new PlayerInventory(null);
		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = player.getInventory().getStack(slot);
			if (EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE))
				continue;
			if (!EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP))
				inventory.setStack(slot, stack);
		}
		return inventory;
	}

	public static void unequipPlayer(PlayerEntity player) {
		for (int slot = 0; slot < player.getInventory().size(); slot++) {
			ItemStack stack = player.getInventory().getStack(slot);
			if (!EnchantmentHelper.hasAnyEnchantmentsWith(stack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE))
				player.getInventory().setStack(slot, ItemStack.EMPTY);
		}
	}

	public ActionResult retrieveItems(PlayerEntity player) {
		UUID uuid = player.getUuid();
		if (!this.playerInventories.containsKey(uuid)) {
			player.sendMessage(Text.translatable("block.wordstones.drop_box.inventory_not_exists"), true);
			return ActionResult.FAIL;
		}

		PlayerInventory inventory = this.playerInventories.get(uuid);
		equipPlayer(inventory, player);
		this.playerInventories.remove(uuid);

		markDirty();
		return ActionResult.SUCCESS;
	}

	public static void equipPlayer(PlayerInventory inventory, PlayerEntity player) {
		for (int slot = 0; slot < inventory.size(); slot++) {
			ItemStack stack = inventory.getStack(slot);
			if (player.getInventory().getStack(slot).isEmpty()) {
//				player.getInventory().offerOrDrop(stack);
				player.getInventory().setStack(slot, stack);
				if (player instanceof ServerPlayerEntity serverPlayer)
					serverPlayer.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-2, 0, slot, stack));
			} else {
				player.getInventory().offerOrDrop(stack);
			}
		}
	}

}
