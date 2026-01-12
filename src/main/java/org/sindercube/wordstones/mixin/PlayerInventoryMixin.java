package org.sindercube.wordstones.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryWrapper;
import org.sindercube.wordstones.content.entity.WordstonesPlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin implements WordstonesPlayerInventory {

	@Unique private RegistryWrapper.WrapperLookup registryLookup;

	@WrapOperation(
		method = {"writeNbt", "readNbt"},
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/entity/player/PlayerEntity;getRegistryManager()Lnet/minecraft/registry/DynamicRegistryManager;"
		)
	)
	public DynamicRegistryManager changeRegistryLookup(PlayerEntity player, Operation<DynamicRegistryManager> original) {
		if (player != null) return original.call(player);
		return (DynamicRegistryManager) this.getRegistryLookup();
	}

	@Override
	public void setRegistryLookup(RegistryWrapper.WrapperLookup registryLookup) {
		this.registryLookup = registryLookup;
	}

	@Override
	public RegistryWrapper.WrapperLookup getRegistryLookup() {
		return this.registryLookup;
	}

}
