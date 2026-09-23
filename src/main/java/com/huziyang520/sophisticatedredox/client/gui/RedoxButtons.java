package com.huziyang520.sophisticatedredox.client.gui;

import com.huziyang520.sophisticatedredox.Constants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinitions;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.TextureBlitData;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.UV;

import java.util.Map;

/**
 * The switch definitions this mod adds to the settings page.
 *
 * <p>Button <b>backgrounds</b> are reused straight from Sophisticated Core's public helpers
 * ({@link ButtonDefinitions#createToggleButtonDefinition}), so the switches look and hover like every other
 * upgrade's switches. Sophisticated Core has no foreground icon for "level Ⅰ-Ⅳ", "dewax" or "block shape", so
 * those eight 16×16 icons are our own - pointing into our own 256×256 atlas, which is the same layout convention
 * Sophisticated Core uses for its own {@code icons.png}.
 */
public final class RedoxButtons {
	private static final Identifier ICONS = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/redox_icons.png");
	/** 16×16 icons are drawn 1px inside an 18×18 button, exactly like Sophisticated Core's own switches. */
	private static final Position ICON_OFFSET = new Position(1, 1);

	private RedoxButtons() {
	}

	/**
	 * The level switch: one state per selectable step count. Sophisticated Core's own {@code SORT_BY} (4 states) and
	 * {@code RepeatMode} (3 states) switches work the same way - clicking advances to the next state.
	 */
	public static ButtonDefinition.Toggle<Integer> level() {
		return ButtonDefinitions.createToggleButtonDefinition(Map.of(
				1, state(new UV(0, 0), "level.1"),
				2, state(new UV(16, 0), "level.2"),
				3, state(new UV(32, 0), "level.3"),
				4, state(new UV(48, 0), "level.4")));
	}

	/** Whether a reduction upgrade may strip wax (as its first step) instead of skipping waxed blocks. */
	public static ButtonDefinition.Toggle<Boolean> dewax() {
		return ButtonDefinitions.createToggleButtonDefinition(ButtonDefinitions.getBooleanStateData(
				state(new UV(0, 16), "dewax.on"),
				state(new UV(16, 16), "dewax.off")));
	}

	/** Whether the filter compares whole copper block shapes instead of exact items. */
	public static ButtonDefinition.Toggle<Boolean> shapeMatch() {
		return ButtonDefinitions.createToggleButtonDefinition(ButtonDefinitions.getBooleanStateData(
				state(new UV(32, 16), "shape_match.on"),
				state(new UV(48, 16), "shape_match.off")));
	}

	private static ToggleButton.StateData state(UV uv, String tooltipKey) {
		return new ToggleButton.StateData(
				new TextureBlitData(ICONS, ICON_OFFSET, Dimension.SQUARE_256, uv, Dimension.SQUARE_16),
				Component.translatable("gui." + Constants.MOD_ID + ".button." + tooltipKey));
	}
}
