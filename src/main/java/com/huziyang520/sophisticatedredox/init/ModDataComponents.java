package com.huziyang520.sophisticatedredox.init;

import com.huziyang520.sophisticatedredox.Constants;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * The per-upgrade settings, stored on the upgrade item stack itself.
 *
 * <p>Shaped exactly like {@code sophisticatedbackpacks}' own {@code ModDataComponents} (which is the closest thing
 * to a reference for a third-party addon): {@code persistent} for saving, {@code networkSynchronized} so the
 * setting reaches a client that only knows the upgrade through a synced stack.
 *
 * <p>Because the settings live on the stack, two upgrades of the same type inside one backpack can be configured
 * independently - no extra work needed for that.
 */
public final class ModDataComponents {
	private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
			DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, Constants.MOD_ID);

	/** Reduction upgrades only: may a waxed block be dewaxed (as the first step) instead of being skipped? */
	public static final Supplier<DataComponentType<Boolean>> DEWAX_ALLOWED = DATA_COMPONENT_TYPES.register("dewax_allowed",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	/** Advanced upgrades only: does the filter compare block "shapes" instead of exact items? */
	public static final Supplier<DataComponentType<Boolean>> SHAPE_MATCH = DATA_COMPONENT_TYPES.register("shape_match",
			() -> new DataComponentType.Builder<Boolean>().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());

	/** Advanced upgrades only: how many single conversion steps to perform per insertion (Ⅰ-Ⅳ). */
	public static final Supplier<DataComponentType<Integer>> REDOX_STEPS = DATA_COMPONENT_TYPES.register("redox_steps",
			() -> new DataComponentType.Builder<Integer>().persistent(Codec.INT).networkSynchronized(ByteBufCodecs.INT).build());

	private ModDataComponents() {
	}

	public static void register(IEventBus modBus) {
		DATA_COMPONENT_TYPES.register(modBus);
	}
}
