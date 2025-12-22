package org.sindercube.wordstones.content;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public record Word(String value) {

	public static final Codec<Word> CODEC = Codec.STRING.xmap(Word::new, Word::value);

	public static final PacketCodec<ByteBuf, Word> PACKET_CODEC = PacketCodecs.STRING.xmap(Word::new, Word::value);

	public Word(String value) {
		this.value = StringUtils.substring(value, 0, 4).toUpperCase();
	}

	@Override
	public @NotNull String toString() {
		return this.value;
	}

}
