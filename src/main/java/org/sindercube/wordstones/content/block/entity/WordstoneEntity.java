package org.sindercube.wordstones.content.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.sindercube.wordstones.content.GlobalWordstones;
import org.sindercube.wordstones.content.Word;
import org.solstice.euclidsElements.util.Location;
import org.sindercube.wordstones.registry.WordstoneBlockEntityTypes;

public class WordstoneEntity extends BlockEntity {

    @Nullable protected Word word;
	public int ticks;

	public WordstoneEntity(BlockPos pos, BlockState state) {
        super(WordstoneBlockEntityTypes.WORDSTONE, pos, state);
    }

	@Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        if (nbt.contains("word")) this.word = new Word(nbt.getString("word"));
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        if (this.word != null) nbt.putString("word", this.word.value());
    }

	public boolean hasWord() {
		return this.word != null;
	}

	@Nullable
    public Word getWord() {
        return this.word;
    }

    public void setWord(Word word) {
        this.word = word;
		markDirty();

		World world = this.getWorld();
		if (world != null) {
			GlobalWordstones.addGlobalWordstone(world, word, new Location(this.getPos(), world.getRegistryKey()));
		}
	}

	@Override
    public void markRemoved() {
		super.markRemoved();
		World world = this.getWorld();
		if (world != null && this.word != null) {
			GlobalWordstones.removeGlobalWordstone(world, this.word);
		}
    }

	public boolean isPlayerTooFar(PlayerEntity player) {
		return !player.canInteractWithBlockAt(this.getPos(), 4);
	}

	public static void tick(World world, BlockPos pos, BlockState state, WordstoneEntity wordstone) {
		wordstone.ticks++;
	}

}
