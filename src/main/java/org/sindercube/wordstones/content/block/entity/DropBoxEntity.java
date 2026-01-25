package org.sindercube.wordstones.content.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
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

	public ActionResult depositItems(PlayerEntity player) {
		UUID uuid = player.getUuid();
		if (this.inventories.containsKey(uuid)) {
			player.sendMessage(Text.translatable("block.wordstones.drop_box.inventory_exists"), true);
			return ActionResult.FAIL;
		}

		DropBoxInventory newInventory = DropBoxInventory.copyFromPlayer(player);
		if (newInventory.isEmpty()) return ActionResult.FAIL;

		this.inventories.put(uuid, newInventory);
		unequipPlayer(player);

		markDirty();
		return ActionResult.SUCCESS;
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
		if (!this.inventories.containsKey(uuid)) {
			player.sendMessage(Text.translatable("block.wordstones.drop_box.inventory_not_exists"), true);
			return ActionResult.FAIL;
		}

		DropBoxInventory inventory = this.inventories.get(uuid);
		equipPlayer(inventory, player);
		this.inventories.remove(uuid);

		markDirty();
		return ActionResult.SUCCESS;
	}

	public static void equipPlayer(DropBoxInventory inventory, PlayerEntity player) {
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
