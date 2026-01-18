package org.sindercube.wordstones.content.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;
import org.sindercube.wordstones.content.state.GlobalWordstoneManager;
import org.sindercube.wordstones.util.Location;

import java.util.Map;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class WordstoneCommand {

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess access) {
		LiteralArgumentBuilder<ServerCommandSource> builder = literal("wordstone")
			.requires(source -> source.hasPermissionLevel(2));

		builder.then(literal("list")
			.executes(WordstoneCommand::executeList)
		);
		builder.then(literal("teleport")
			.then(CommandManager.argument("target", EntityArgumentType.player()))
				.then(argument("word", WordArgumentType.word()))
					.executes(WordstoneCommand::executeTeleport)
		);

		dispatcher.register(builder);
	}

	private static int executeList(CommandContext<ServerCommandSource> context) {
		ServerWorld world = context.getSource().getWorld();
		Map<Word, Location> wordstones = GlobalWordstoneManager.get(world).getData();
		if (wordstones.isEmpty()) {
			Text message = Text.translatable("commands.wordstone.list.empty");
			context.getSource().sendFeedback(() -> message, true);
			return 0;
		}

		MutableText message = Text.translatable("commands.wordstone.list");
		wordstones.forEach((word, location) -> {
			Text locationText = Text.translatable("gui.location",
				Text.literal(String.valueOf(location.pos().getX())),
				Text.literal(String.valueOf(location.pos().getY())),
				Text.literal(String.valueOf(location.pos().getZ())),
				location.getDimensionName()
			);
			message.append(ScreenTexts.LINE_BREAK)
				.append(Text.translatable("commands.wordstone.list.entry", Text.literal(word.value()), locationText));
		});
		context.getSource().sendFeedback(() -> message, true);
		return 1;
	}

	private static int executeTeleport(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerWorld world = context.getSource().getWorld();
		PlayerEntity target = EntityArgumentType.getPlayer(context, "target");
		Word word = WordArgumentType.getWord(context, "word");
		if (!GlobalWordstoneManager.get(world).getData().containsKey(word)) {
			context.getSource().sendFeedback(() -> Text.translatable("commands.wordstone.teleport.error.no_wordstone"), true);
			return 0;
		}
		WordstoneEntity.teleportToWordstone(world, target, word);
		context.getSource().sendFeedback(() -> Text.translatable("commands.wordstone.teleport.success", Text.literal(word.value())), true);
		return 1;
	}

}
