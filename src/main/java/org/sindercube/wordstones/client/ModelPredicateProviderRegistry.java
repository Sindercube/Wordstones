package org.sindercube.wordstones.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public class ModelPredicateProviderRegistry {

	protected record PredicateData(Class<? extends Item> clazz, Identifier id, ClampedModelPredicateProvider provider) {}

	protected static final List<PredicateData> PREDICATES = new ArrayList<>();

	public static void register(Class<? extends Item> itemClass, Identifier id, ClampedModelPredicateProvider provider) {
		PREDICATES.add(new PredicateData(itemClass, id, provider));
	}

	public static void init() {
		Registries.ITEM.forEach(ModelPredicateProviderRegistry::register);
		PREDICATES.clear();
	}

	protected static void register(Item item) {
		Predicate<PredicateData> isAssignable = data -> data.clazz.isAssignableFrom(item.getClass());
		Consumer<PredicateData> register = data -> net.minecraft.client.item.ModelPredicateProviderRegistry.register(item, data.id, data.provider);
		PREDICATES.stream().filter(isAssignable).forEach(register);
	}

}
