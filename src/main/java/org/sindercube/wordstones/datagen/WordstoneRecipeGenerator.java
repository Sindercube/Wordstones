package org.sindercube.wordstones.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.OnKilledCriterion;
import net.minecraft.block.Blocks;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import org.solstice.euclidsElements.autoDatagen.api.generator.AdvancedRecipeProvider;
import org.sindercube.wordstones.registry.WordstoneBlocks;
import org.sindercube.wordstones.registry.WordstoneItems;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class WordstoneRecipeGenerator extends AdvancedRecipeProvider {

	public WordstoneRecipeGenerator(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
		super(output, registryLookup);
	}

	@Override
	public void generate(RecipeExporter exporter, RegistryWrapper.WrapperLookup registryLookup) {
		ShapedRecipeJsonBuilder
			.create(RecipeCategory.DECORATIONS, WordstoneBlocks.DROP_BOX)
			.input('I', Items.IRON_INGOT)
			.input('L', Items.LAPIS_LAZULI)
			.pattern("ILI")
			.pattern("I I")
			.pattern("ILI")
			.criterion("has_lots_of_items", Criteria.INVENTORY_CHANGED.create(new InventoryChangedCriterion.Conditions(
				Optional.empty(),
				new InventoryChangedCriterion.Conditions.Slots(
					NumberRange.IntRange.atLeast(39),
					NumberRange.IntRange.ANY,
					NumberRange.IntRange.ANY
				),
				List.of()
			)))
			.offerTo(exporter);

		ShapedRecipeJsonBuilder
			.create(RecipeCategory.DECORATIONS, WordstoneBlocks.WORDSTONE)
			.input('#', Blocks.STONE_BRICKS)
			.input('B', Blocks.BOOKSHELF)
			.input('I', WordstoneItems.KNOWLEDGE_FRAGMENT)
			.pattern(" # ")
			.pattern("BIB")
			.pattern("###")
			.criterion("has_knowledge_fragment", conditionsFromItem(WordstoneItems.KNOWLEDGE_FRAGMENT))
			.offerTo(exporter);

		ShapelessRecipeJsonBuilder
			.create(RecipeCategory.TOOLS, WordstoneItems.LAST_WILL)
			.input(Items.ECHO_SHARD)
			.input(Items.PAPER)
			.criterion("has_echo_shard", conditionsFromItem(Items.ECHO_SHARD))
			.criterion("has_died", Criteria.ENTITY_KILLED_PLAYER.create(new OnKilledCriterion.Conditions(
				Optional.empty(),
				Optional.empty(),
				Optional.empty()
			)))
			.offerTo(exporter);
	}

}
