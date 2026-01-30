package org.sindercube.wordstones.content.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.math.BlockPos;
import org.sindercube.wordstones.content.block.SteleBlock;
import org.sindercube.wordstones.registry.WordstonesBlockEntityTypes;

public class SteleEntity extends SignBlockEntity {

	public SteleEntity(BlockPos pos, BlockState state) {
		super(WordstonesBlockEntityTypes.STELE, pos, state);
	}

	@Override
	public int getTextLineHeight() {
		return 16;
	}

	@Override
	public int getMaxTextWidth() {
		return 24;
	}

	public boolean hasTopLine() {
		SlabType type = this.getCachedState().get(SteleBlock.TYPE);
		return type == SlabType.TOP || type == SlabType.DOUBLE;
	}

	public boolean hasBottomLine() {
		SlabType type = this.getCachedState().get(SteleBlock.TYPE);
		return type == SlabType.BOTTOM || type == SlabType.DOUBLE;
	}

}
