package com.huziyang520.sophisticatedredox.init;

import com.huziyang520.sophisticatedredox.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModCreativeTabs {
	private static final DeferredRegister<CreativeModeTab> TABS =
			DeferredRegister.create(Registries.CREATIVE_MODE_TAB.identifier(), Constants.MOD_ID);

	public static final Supplier<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
			.icon(() -> new ItemStack(ModItems.OXIDATION_UPGRADE.get()))
			.title(Component.translatable("itemGroup." + Constants.MOD_ID))
			.displayItems((parameters, output) -> {
				output.accept(ModItems.OXIDATION_UPGRADE.get());
				output.accept(ModItems.ADVANCED_OXIDATION_UPGRADE.get());
				output.accept(ModItems.REDUCTION_UPGRADE.get());
				output.accept(ModItems.ADVANCED_REDUCTION_UPGRADE.get());
				output.accept(ModItems.WAXING_UPGRADE.get());
			}).build());

	private ModCreativeTabs() {
	}

	static void register(IEventBus modBus) {
		TABS.register(modBus);
	}
}
