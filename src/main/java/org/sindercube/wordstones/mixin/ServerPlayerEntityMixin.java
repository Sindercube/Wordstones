package org.sindercube.wordstones.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.sindercube.wordstones.util.ExtraPlayerEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends LivingEntity {

	protected ServerPlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "onDeath", at = @At("HEAD"))
	public void beforeDeath(CallbackInfo info, @Local(argsOnly = true) DamageSource source) {
		if (!this.isRemoved() && !this.dead)
			ExtraPlayerEvents.BEFORE_DEATH.invoker().afterDeath((PlayerEntity)(Object)this, source);
	}

}
