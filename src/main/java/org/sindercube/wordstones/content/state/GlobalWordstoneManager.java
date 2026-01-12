package org.sindercube.wordstones.content.state;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import org.sindercube.wordstones.content.Word;
import org.sindercube.wordstones.util.Location;

import java.util.HashMap;
import java.util.Map;

public class GlobalWordstoneManager extends PersistentState {

	public static final Codec<Map<Word, Location>> CODEC = Codec.unboundedMap(Word.CODEC, Location.CODEC);

	public static GlobalWordstoneManager get(ServerWorld world) {
		return world.getServer().getOverworld().getPersistentStateManager().getOrCreate(GlobalWordstoneManager.getPersistentStateType(), "wordstones:global_wordstones");
	}

	public static Type<GlobalWordstoneManager> getPersistentStateType() {
		return new Type<>(
			GlobalWordstoneManager::new,
			GlobalWordstoneManager::fromNbt,
			null
		);
	}

	protected final Map<Word, Location> data;

	public GlobalWordstoneManager() {
		this.data = new HashMap<>();
	}

	public Map<Word, Location> getData() {
		return this.data;
	}

	public void set(Word word, Location location) {
		this.data.put(word, location);
		this.markDirty();
	}

	public void remove(Word word) {
		this.data.remove(word);
		this.markDirty();
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		System.out.println(this);
		NbtCompound result = (NbtCompound) CODEC.encodeStart(NbtOps.INSTANCE, this.data).getOrThrow();
		System.out.println(result);
		return result;
	}

	public static GlobalWordstoneManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		GlobalWordstoneManager manager = new GlobalWordstoneManager();
		manager.data.putAll(CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow());
		return manager;
	}

}
