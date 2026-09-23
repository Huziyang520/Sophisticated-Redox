package com.huziyang520.sophisticatedredox.init;

import com.huziyang520.sophisticatedredox.Constants;
import com.huziyang520.sophisticatedredox.upgrade.items.AdvancedOxidationUpgradeItem;
import com.huziyang520.sophisticatedredox.upgrade.items.AdvancedReductionUpgradeItem;
import com.huziyang520.sophisticatedredox.upgrade.items.OxidationUpgradeItem;
import com.huziyang520.sophisticatedredox.upgrade.items.ReductionUpgradeItem;
import com.huziyang520.sophisticatedredox.upgrade.items.WaxingUpgradeItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);

	public static final DeferredHolder<Item, OxidationUpgradeItem> OXIDATION_UPGRADE =
			ITEMS.registerItem("oxidation_upgrade", OxidationUpgradeItem::new);
	public static final DeferredHolder<Item, AdvancedOxidationUpgradeItem> ADVANCED_OXIDATION_UPGRADE =
			ITEMS.registerItem("advanced_oxidation_upgrade", AdvancedOxidationUpgradeItem::new);
	public static final DeferredHolder<Item, ReductionUpgradeItem> REDUCTION_UPGRADE =
			ITEMS.registerItem("reduction_upgrade", ReductionUpgradeItem::new);
	public static final DeferredHolder<Item, AdvancedReductionUpgradeItem> ADVANCED_REDUCTION_UPGRADE =
			ITEMS.registerItem("advanced_reduction_upgrade", AdvancedReductionUpgradeItem::new);
	public static final DeferredHolder<Item, WaxingUpgradeItem> WAXING_UPGRADE =
			ITEMS.registerItem("waxing_upgrade", WaxingUpgradeItem::new);

	private ModItems() {
	}

	public static void register(IEventBus modBus) {
		ITEMS.register(modBus);
		ModCreativeTabs.register(modBus);
	}
}
