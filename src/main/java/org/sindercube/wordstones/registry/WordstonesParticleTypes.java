package org.sindercube.wordstones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.particle.SquidInkParticle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class WordstonesParticleTypes {

	@Environment(EnvType.CLIENT)
	public static void clientInit() {
		ParticleFactoryRegistry.getInstance().register(ENCHANTED_INK, SquidInkParticle.Factory::new);
	}

	public static void init() {}

	public static final ParticleType<SimpleParticleType> ENCHANTED_INK = register("enchanted_ink", FabricParticleTypes.simple());

	private static <T extends ParticleEffect> ParticleType<T> register(String name, ParticleType<T> type) {
		return Registry.register(Registries.PARTICLE_TYPE, name, type);
	}

}
