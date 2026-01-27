package org.sindercube.wordstones.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.sindercube.wordstones.Wordstones;

public class WordstonesSoundEvents {


	public static void init() {}

	public static final SoundEvent ITEM_LINKED_TOME_LINK = register("item.linked_tome.link");
	public static final SoundEvent ITEM_ENCHANTED_QUILL_USE_SIGN = register("item.enchanted_quill.use.sign");
	public static final RegistryEntry<SoundEvent> DROP_BOX_DEPOSIT = registerReference("block.drop_box.deposit");
	public static final RegistryEntry<SoundEvent> DROP_BOX_RETRIEVE = registerReference("block.drop_box.retrieve");
	public static final RegistryEntry<SoundEvent> ENTITY_PLAYER_TELEPORT = registerReference("entity.player.teleport");

	protected static SoundEvent register(String name) {
		Identifier id = Wordstones.of(name);
		return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

	protected static RegistryEntry<SoundEvent> registerReference(String name) {
		Identifier id = Wordstones.of(name);
		return Registry.registerReference(Registries.SOUND_EVENT, id, SoundEvent.of(id));
	}

}
