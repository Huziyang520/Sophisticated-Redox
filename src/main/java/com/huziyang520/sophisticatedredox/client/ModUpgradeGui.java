package com.huziyang520.sophisticatedredox.client;

import com.huziyang520.sophisticatedredox.client.gui.RedoxUpgradeTab;
import com.huziyang520.sophisticatedredox.init.ModUpgradeContainers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeGuiManager;

/**
 * Client-only registration. Must only ever be touched when {@code Dist.CLIENT} (see the mod constructor), because
 * it links against Sophisticated Core's client GUI classes which do not exist on a dedicated server.
 */
public final class ModUpgradeGui {
	private ModUpgradeGui() {
	}

	public static void init(IEventBus modBus) {
		modBus.addListener(ModUpgradeGui::onRegisterMenuScreens);
	}

	private static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
		UpgradeGuiManager.registerTab(ModUpgradeContainers.REDOX_TYPE, RedoxUpgradeTab::new);
	}
}
