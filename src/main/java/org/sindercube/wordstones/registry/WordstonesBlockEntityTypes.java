package org.sindercube.wordstones.registry;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.sindercube.wordstones.Wordstones;
import org.sindercube.wordstones.content.block.entity.DropBoxEntity;
import org.sindercube.wordstones.content.block.entity.SteleEntity;
import org.sindercube.wordstones.content.block.entity.WordstoneEntity;

public class WordstonesBlockEntityTypes {

	public static void init() {}

	public static final BlockEntityType<WordstoneEntity> WORDSTONE = register("wordstone",
		BlockEntityType.Builder.create(WordstoneEntity::new, WordstonesBlocks.WORDSTONE)
	);
	public static final BlockEntityType<DropBoxEntity> DROP_BOX = register("drop_box",
		BlockEntityType.Builder.create(DropBoxEntity::new, WordstonesBlocks.DROP_BOX)
	);
	public static final BlockEntityType<SteleEntity> STELE = register("stele",
		BlockEntityType.Builder.create(SteleEntity::new, WordstonesBlocks.STONE_STELE, WordstonesBlocks.DEEPSLATE_STELE, WordstonesBlocks.SANDSTONE_STELE, WordstonesBlocks.RED_SANDSTONE_STELE)
	);

	public static <T extends BlockEntity> BlockEntityType<T> register(String name, BlockEntityType.Builder<T> builder) {
		Identifier id = Wordstones.of(name);
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, id, builder.build());
	}

}
