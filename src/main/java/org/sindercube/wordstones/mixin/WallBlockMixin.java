package org.sindercube.wordstones.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBlock;
import net.minecraft.util.math.Direction;
import org.sindercube.wordstones.content.block.SteleBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WallBlock.class)
public class WallBlockMixin {

	@WrapMethod(method="shouldConnectTo")
	boolean shouldConnectTo(BlockState state, boolean faceFullSquare, Direction side, Operation<Boolean> original) {
		if (state.getBlock() instanceof SteleBlock) return true;
		return original.call(state, faceFullSquare, side);
	}

}
