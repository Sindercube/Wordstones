package org.sindercube.wordstones.registry;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.GlobalPos;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.util.Location;

public class WordstonesComponentTypes {

	public static void init() {}

	public static final ComponentType<Location> LOCATION = register("location",
		Location.CODEC,
		Location.PACKET_CODEC
	);

	public static final ComponentType<Word> WORD = register("word",
		Word.CODEC,
		Word.PACKET_CODEC
	);

	public static final ComponentType<GlobalPos> GLOBAL_POSITION = register("global_position",
		GlobalPos.CODEC,
		GlobalPos.PACKET_CODEC
	);

	private static <T> ComponentType<T> register(String name, Codec<T> codec) {
		return register(name, codec, PacketCodecs.registryCodec(codec));
	}

	private static <T> ComponentType<T> register(String name, Codec<T> codec, PacketCodec<? super RegistryByteBuf, T> packetCodec) {
		ComponentType<T> component = ComponentType.<T>builder()
			.codec(codec)
			.packetCodec(packetCodec)
			.cache()
			.build();
		return Registry.register(Registries.DATA_COMPONENT_TYPE, Wordstones.of(name), component);
	}

}
