package org.sindercube.wordstones.util;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;

public class ExtraPlayerEvents {

	public static final Event<BeforeDeath> BEFORE_DEATH = EventFactory.createArrayBacked(BeforeDeath.class,
		callbacks -> (entity, damageSource) -> {
			for (BeforeDeath callback : callbacks) callback.afterDeath(entity, damageSource);
	});

	@FunctionalInterface
	public interface BeforeDeath {
		void afterDeath(PlayerEntity entity, DamageSource damageSource);
	}

}
