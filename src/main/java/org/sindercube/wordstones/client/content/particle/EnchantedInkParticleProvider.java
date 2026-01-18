package org.sindercube.wordstones.client.content.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public class EnchantedInkParticleProvider implements ParticleFactory<SimpleParticleType> {

	private final SpriteProvider spriteProvider;

	public EnchantedInkParticleProvider(SpriteProvider spriteProvider) {
		this.spriteProvider = spriteProvider;
	}

	@Override
	public @Nullable Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
		return null;
	}

//	public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
//		return new SquidInkParticle(world, x, y, z, velocityX, velocityY, velocityZ, ColorHelper.Argb.getArgb(255, 255, 255, 255), this.spriteProvider);
//	}

}
