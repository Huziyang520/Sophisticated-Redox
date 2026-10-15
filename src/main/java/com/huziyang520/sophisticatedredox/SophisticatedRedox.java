package com.huziyang520.sophisticatedredox;

import com.huziyang520.sophisticatedredox.client.ModConfigGui;
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

import java.util.Arrays;

@Mod(Constants.MOD_ID)
public class SophisticatedRedox {
	@SuppressWarnings("java:S1118") // has to be public for the mod to load
	public SophisticatedRedox(IEventBus modBus, Dist dist, ModContainer container) {
		container.registerConfig(syncedConfigType(), RedoxConfig.SPEC);

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

		// Client-only: both of these link against client classes (Sophisticated Core's client GUI, NeoForge's
		// ConfigurationScreen). Keeping them behind the dist check keeps them off the dedicated-server classpath.
		if (dist == Dist.CLIENT) {
			ModConfigGui.register(container);
			ModUpgradeGui.init(modBus);
		}

		Constants.LOG.info("Sophisticated Redox loaded: 5 copper redox upgrades registered");
	}

	/**
	 * The config type this mod registers: the server-owned config that is pushed to clients (gameplay step
	 * limits plus the diagnostics switch), i.e. what the loader used to call {@code ModConfig.Type.SERVER}.
	 *
	 * <p>Resolved by <b>name</b> instead of referencing a constant on purpose. The loader renamed the enum
	 * constants <b>inside the 26.3 line</b>: NeoForge 26.3.0.1-beta ships fancymodloader 12.0.0, which still
	 * has {@code COMMON}/{@code SERVER}, while 26.3.0.77 ships 12.0.12, which has {@code LOCAL}/{@code SYNCED}.
	 * NeoForge's own rename of {@code NeoForgeServerConfig} to {@code NeoForgeSyncedConfig} is what pins the
	 * mapping (verified side by side in the two {@code NeoForgeMod} classes). A hard reference to either
	 * constant would therefore fail on the other half of the loader range this jar declares ({@code [26.3,)}),
	 * with a {@code NoSuchFieldError} thrown while the mod is being constructed.
	 */
	private static ModConfig.Type syncedConfigType() {
		for (ModConfig.Type type : ModConfig.Type.values()) {
			if ("SYNCED".equals(type.name()) || "SERVER".equals(type.name())) {
				return type;
			}
		}
		throw new IllegalStateException("No synced/server config type in " + Arrays.toString(ModConfig.Type.values()));
	}
}
