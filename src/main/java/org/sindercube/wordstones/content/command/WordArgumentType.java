package org.sindercube.wordstones.content.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.text.Text;
import org.sindercube.wordstones.content.Word;

import java.util.Collection;
import java.util.List;

public class WordArgumentType implements ArgumentType<Word> {

	public static final List<String> EXAMPLES = List.of("WORD", "MINE", "CRFT", "READ", "BOOK");

	public static final SimpleCommandExceptionType INVALID_LENGTH_EXCEPTION =
			new SimpleCommandExceptionType(Text.translatable("argument.word.invalid"));

	public static WordArgumentType word() {
		return new WordArgumentType();
	}

	public static Word getWord(CommandContext<?> context, String name) {
		return context.getArgument(name, Word.class);
	}

	@Override
	public Word parse(StringReader reader) throws CommandSyntaxException {
		String string = reader.readUnquotedString();
		if (string.length() != 4) throw INVALID_LENGTH_EXCEPTION.create();

		return new Word(reader.readUnquotedString());
	}

//	@Override
//	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
//		ServerWorld world = ((ServerCommandSource)context.getSource()).getWorld();
//		List<String> words = GlobalWordstoneManager.get(world).getData().keySet().stream()
//			.map(Word::value)
//			.toList();
//		return CommandSource.suggestMatching(words, builder);
//	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}

	@Override
	public String toString() {
		return "word()";
	}

}
