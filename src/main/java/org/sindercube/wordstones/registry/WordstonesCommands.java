package org.sindercube.wordstones.registry;

import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.content.command.WordArgumentType;
import org.sindercube.wordstones.content.command.WordstoneCommand;

public class WordstonesCommands {

	public static void init() {
		ArgumentTypeRegistry.registerArgumentType(
			Wordstones.of("word"),
			WordArgumentType.class, ConstantArgumentSerializer.of(WordArgumentType::word)
		);
		CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> {
			WordstoneCommand.register(dispatcher, access);
		});
	}

}
