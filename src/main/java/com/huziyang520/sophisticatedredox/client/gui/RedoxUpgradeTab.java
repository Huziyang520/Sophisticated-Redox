package com.huziyang520.sophisticatedredox.client.gui;

import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeContainer;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.UpgradeSettingsTab;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogicContainer;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogicControl;

/**
 * Client-side settings tab. Reuses Sophisticated Core's {@link FilterLogicControl.Basic}, so the filter renders with
 * the same sprites, layout and whitelist/blacklist toggle as the base mod's own upgrade filters, and Sophisticated
 * Core's {@link ToggleButton}, so the switches behave and hover identically too.
 *
 * <p>The switch row is built from the upgrade's capabilities: 还原 shows "dewax", the advanced ones show
 * "level" and "shape match", 氧化 and 涂蜡 show no switches at all and their filter moves up a row instead of
 * leaving a blank gap under the title.
 *
 * <p>The tab label is taken from the upgrade item itself, so all five upgrades share this one class.
 */
public class RedoxUpgradeTab extends UpgradeSettingsTab<RedoxUpgradeContainer> {
	private static final int BUTTON_STEP = 18;
	private static final int BUTTON_ROW_OFFSET = 24;
	private static final int FILTER_BELOW_BUTTONS_OFFSET = 44;

	private final FilterLogicControl<FilterLogic, FilterLogicContainer<FilterLogic>> filterLogicControl;

	public RedoxUpgradeTab(RedoxUpgradeContainer container, Position position, StorageScreenBase<?> screen) {
		super(container, position, screen, container.getUpgradeStack().getHoverName(),
				Component.translatable(container.getUpgradeStack().getItem().getDescriptionId() + ".tooltip"));

		int buttonX = x + 3;
		int buttonY = y + BUTTON_ROW_OFFSET;
		int buttonCount = 0;

		if (container.supportsSteps()) {
			addHideableChild(new ToggleButton<>(new Position(buttonX, buttonY), RedoxButtons.level(),
					button -> container.cycleSteps(), container::getSteps));
			buttonX += BUTTON_STEP;
			buttonCount++;
		}
		if (container.supportsDewax()) {
			addHideableChild(new ToggleButton<>(new Position(buttonX, buttonY), RedoxButtons.dewax(),
					button -> container.setDewaxAllowed(!container.isDewaxAllowed()), container::isDewaxAllowed));
			buttonX += BUTTON_STEP;
			buttonCount++;
		}
		if (container.supportsShapeMatch()) {
			addHideableChild(new ToggleButton<>(new Position(buttonX, buttonY), RedoxButtons.shapeMatch(),
					button -> container.setShapeMatch(!container.isShapeMatch()), container::isShapeMatch));
			buttonCount++;
		}

		// Only skip the button row when there is no switch at all - an empty row would just be a gap under the title.
		int filterY = buttonCount > 0 ? y + FILTER_BELOW_BUTTONS_OFFSET : y + BUTTON_ROW_OFFSET;
		filterLogicControl = addHideableChild(new FilterLogicControl.Basic(screen, new Position(x + 3, filterY),
				container.getFilterLogicContainer(), container.getFilterSlotsInRow()));
	}

	@Override
	protected void moveSlotsToTab() {
		filterLogicControl.moveSlotsToView();
	}
}
