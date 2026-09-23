package com.huziyang520.sophisticatedredox.init;

import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeContainer;
import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeWrapper;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerRegistry;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;

/**
 * All five upgrades share one container/tab pair, so each container type is registered under the five item ids.
 * Sophisticated Core looks the container up by the upgrade item's registry name.
 */
public final class ModUpgradeContainers {
	public static final UpgradeContainerType<RedoxUpgradeWrapper, RedoxUpgradeContainer> REDOX_TYPE =
			new UpgradeContainerType<>(RedoxUpgradeContainer::new);

	private ModUpgradeContainers() {
	}

	public static void registerContainers(RegisterEvent event) {
		if (!event.getRegistryKey().equals(Registries.MENU)) {
			return;
		}
		UpgradeContainerRegistry.register(ModItems.OXIDATION_UPGRADE.getId(), REDOX_TYPE);
		UpgradeContainerRegistry.register(ModItems.ADVANCED_OXIDATION_UPGRADE.getId(), REDOX_TYPE);
		UpgradeContainerRegistry.register(ModItems.REDUCTION_UPGRADE.getId(), REDOX_TYPE);
		UpgradeContainerRegistry.register(ModItems.ADVANCED_REDUCTION_UPGRADE.getId(), REDOX_TYPE);
		UpgradeContainerRegistry.register(ModItems.WAXING_UPGRADE.getId(), REDOX_TYPE);
	}
}
