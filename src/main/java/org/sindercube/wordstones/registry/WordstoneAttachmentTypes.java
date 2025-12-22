package org.sindercube.wordstones.registry;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.content.GlobalWordstones;
import org.sindercube.wordstones.content.Word;
import org.solstice.euclidsElements.util.Location;

import java.util.Map;
import java.util.function.Consumer;

public class WordstoneAttachmentTypes {

	public static void init() {}

	public static final AttachmentType<Map<Word, Location>> GLOBAL_WORDSTONES = register("global_wordstones",
		builder -> builder
			.persistent(GlobalWordstones.CODEC)
//			.syncWith(GlobalWordstones.PACKET_CODEC, AttachmentSyncPredicate.all())
	);

	public static <T> AttachmentType<T> register(String name, Consumer<AttachmentRegistry.Builder<T>> consumer) {
		return AttachmentRegistry.create(Wordstones.of(name), consumer);
	}

}
