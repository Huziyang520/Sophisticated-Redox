package com.huziyang520.sophisticatedredox.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client-only: hands NeoForge's own configuration screen to the mod list, so Sophisticated Redox shows up with a
 * "Config" button like every other NeoForge mod. This is the same registration the base mods use
 * ({@code SophisticatedCore} / {@code SophisticatedBackpacks} register {@code ConfigurationScreen::new} through
 * {@link IConfigScreenFactory}), so the page looks and behaves exactly like theirs - no Cloth Config dependency.
 *
 * <p>The screen builds itself from {@code ModConfigSpec} metadata: translations come from
 * {@code sophisticated_redox.configuration.*}, and the spec's own comments become the tooltips, so the entries need
 * no per-entry registration here.
 *
 * <p>Must only be touched when {@code Dist.CLIENT} (see the mod constructor): {@link ConfigurationScreen} is a
 * client class.
 */
public final class ModConfigGui {
	private ModConfigGui() {
	}

	public static void register(ModContainer container) {
		// "Configured" provides its own config UI for every mod; registering ours as well would show two buttons.
		if (ModList.get().isLoaded("configured")) {
			return;
		}
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}
}
