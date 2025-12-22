package org.sindercube.wordstones.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.content.block.DropBoxBlock;
import org.sindercube.wordstones.content.block.WordstoneBlock;

import java.util.function.Function;

public class WordstoneBlocks {

	public static void init() {}

	@Environment(EnvType.CLIENT)
	public static void clientInit() {
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
			WORDSTONE,
			DROP_BOX
		);
	}

	public static final Block WORDSTONE = register("wordstone",
		WordstoneBlock::new,
		AbstractBlock.Settings.create()
			.requiresTool()
			.strength(5, 12)
			.mapColor(MapColor.STONE_GRAY)
			.sounds(BlockSoundGroup.STONE)
	);
	public static final Block DROP_BOX = register("drop_box",
		DropBoxBlock::new,
		AbstractBlock.Settings.create()
			.requiresTool()
			.strength(5, 6)
			.mapColor(MapColor.IRON_GRAY)
			.instrument(NoteBlockInstrument.IRON_XYLOPHONE)
			.sounds(BlockSoundGroup.METAL)
	);

	public static Block register(String name) {
		return register(name, Block::new, AbstractBlock.Settings.create());
	}

	public static Block register(String name, AbstractBlock.Settings settings) {
		return register(name, Block::new, settings);
	}

	public static Block register(String name, Function<AbstractBlock.Settings, Block> function, AbstractBlock.Settings settings) {
		return register(name, function, settings, new Item.Settings());
	}

	public static Block register(String name, Function<AbstractBlock.Settings, Block> function, AbstractBlock.Settings blockSettings, Item.Settings itemSettings) {
		Identifier id = Wordstones.of(name);
		RegistryKey<Block> blockKey = RegistryKey.of(RegistryKeys.BLOCK, id);
		Block block = function.apply(blockSettings);
		Registry.register(Registries.BLOCK, blockKey, block);
		RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
		Item item = new BlockItem(block, itemSettings);
		Registry.register(Registries.ITEM, itemKey, item);
		return block;
	}

}
