package org.sindercube.wordstones.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.sindercube.wordstones.registry.WordstoneItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EyeOfEnderEntity.class)
public abstract class EyeOfEnderEntityMixin extends Entity implements FlyingItemEntity {

	public EyeOfEnderEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@WrapOperation(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/World;syncWorldEvent(ILnet/minecraft/util/math/BlockPos;I)V"
		)
	)
	void dropKnowledgeFragment(World instance, int id, BlockPos blockPos, int data, Operation<Void> original) {
		original.call(instance, id, blockPos, data);
		ItemEntity entity = new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), WordstoneItems.KNOWLEDGE_FRAGMENT.getDefaultStack());
		this.getWorld().spawnEntity(entity);
	}

}
