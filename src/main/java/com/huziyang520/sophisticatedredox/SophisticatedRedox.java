package com.huziyang520.sophisticatedredox;

import com.huziyang520.sophisticatedredox.client.ModUpgradeGui;
import com.huziyang520.sophisticatedredox.config.RedoxConfig;
import com.huziyang520.sophisticatedredox.core.RedoxTickQueue;
import com.huziyang520.sophisticatedredox.core.WaxTable;
import com.huziyang520.sophisticatedredox.init.ModDataComponents;
import com.huziyang520.sophisticatedredox.init.ModItems;
import com.huziyang520.sophisticatedredox.init.ModUpgradeContainers;
import net.minecraft.core.registries.Registries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;

@Mod(Constants.MOD_ID)
public class SophisticatedRedox {
	@SuppressWarnings("java:S1118") // has to be public for the mod to load
	public SophisticatedRedox(IEventBus modBus, Dist dist, ModContainer container) {
		container.registerConfig(ModConfig.Type.SERVER, RedoxConfig.SPEC);

		ModItems.register(modBus);
		ModDataComponents.register(modBus);
		modBus.addListener(ModUpgradeContainers::registerContainers);

		// The wax lookup prefers NeoForge's "neoforge:waxables" data map, which only exists once datapacks have
		// been loaded - so drop the cached table whenever the block registry's data maps are (re)built.
		NeoForge.EVENT_BUS.addListener((DataMapsUpdatedEvent event) -> {
			if (Registries.BLOCK.equals(event.getRegistryKey())) {
				WaxTable.invalidate();
			}
		});

		// Conversions are deferred to the end of the server tick; see RedoxTickQueue for why that is required.
		NeoForge.EVENT_BUS.addListener(RedoxTickQueue::onServerTick);
		NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> RedoxTickQueue.clear());

		// Client-only: ModUpgradeGui links against Sophisticated Core's client GUI classes.
		// The branch keeps that class out of the dedicated-server classpath.
		if (dist == Dist.CLIENT) {
			ModUpgradeGui.init(modBus);
		}

		Constants.LOG.info("Sophisticated Redox loaded: 5 copper redox upgrades registered");
	}
}
