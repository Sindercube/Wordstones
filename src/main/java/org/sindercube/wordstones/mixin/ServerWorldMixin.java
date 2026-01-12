package org.sindercube.wordstones.mixin;

import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

//	@Shadow public abstract PersistentStateManager getPersistentStateManager();

//	@Unique private GlobalWordstoneManager globalWordstoneState = new GlobalWordstoneManager();

//	@Inject(method = "<init>", at = @At("TAIL"))
//	private void setGlobalWordstoneState(CallbackInfo ci) {
//		this.globalWordstoneState = this.getPersistentStateManager().getOrCreate(GlobalWordstoneManager.getPersistentStateType(), "global_wordstones");
//	}

//	@Shadow
//	@NotNull
//	public abstract MinecraftServer getServer();

//	@Override
//	public GlobalWordstoneManager getGlobalWordstoneState() {
//		return this.getServer().getOverworld().getPersistentStateManager().getOrCreate(GlobalWordstoneManager.getPersistentStateType(), "global_wordstones");
//	}

}
