package org.sindercube.wordstones.content.entity;

import net.minecraft.registry.RegistryWrapper;

public interface WordstonesPlayerInventory {

	RegistryWrapper.WrapperLookup getRegistryLookup();

	void setRegistryLookup(RegistryWrapper.WrapperLookup registryLookup);

}
