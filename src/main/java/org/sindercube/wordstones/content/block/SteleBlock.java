package org.sindercube.wordstones.content.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.DirectionProperty;

public class SteleBlock extends AbstractSignBlock {

	public static final WoodType STONE = WoodType.register(
		new WoodType("stone", BlockSetType.STONE, BlockSoundGroup.STONE, BlockSoundGroup.STONE, SoundEvents.BLOCK_FENCE_GATE_CLOSE, SoundEvents.BLOCK_FENCE_GATE_OPEN)
	);

	public static final WoodType DEEPSLATE = WoodType.register(
		new WoodType("deepslate", BlockSetType.STONE, BlockSoundGroup.DEEPSLATE, BlockSoundGroup.DEEPSLATE, SoundEvents.BLOCK_FENCE_GATE_CLOSE, SoundEvents.BLOCK_FENCE_GATE_OPEN)
	);

	public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

	protected SteleBlock(Settings settings) {
		super(WoodType.ACACIA, settings);
	}

	@Override
	protected MapCodec<? extends AbstractSignBlock> getCodec() {
		return createCodec(SteleBlock::new);
	}

	@Override
	public float getRotationDegrees(BlockState state) {
		return state.get(FACING).asRotation();
	}

}
