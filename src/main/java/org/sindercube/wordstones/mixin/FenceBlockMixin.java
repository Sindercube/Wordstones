package org.sindercube.wordstones.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.util.math.Direction;
import org.sindercube.wordstones.content.block.SteleBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FenceBlock.class)
public class FenceBlockMixin {

	@WrapMethod(method = "canConnect")
	public boolean wrapCanConnect(BlockState state, boolean b, Direction d, Operation<Boolean> original) {
		if (state.getBlock() instanceof SteleBlock) return true;
		return original.call(state, b, d);
	}

}
