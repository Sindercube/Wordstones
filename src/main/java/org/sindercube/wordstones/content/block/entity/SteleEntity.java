package org.sindercube.wordstones.content.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.util.math.BlockPos;
import org.sindercube.wordstones.registry.WordstonesBlockEntityTypes;

public class SteleEntity extends SignBlockEntity {

	public SteleEntity(BlockPos pos, BlockState state) {
		super(WordstonesBlockEntityTypes.STELE, pos, state);
	}

}
