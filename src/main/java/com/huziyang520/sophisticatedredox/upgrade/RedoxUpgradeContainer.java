package com.huziyang520.sophisticatedredox.upgrade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerBase;
import net.p3pp3rf1y.sophisticatedcore.common.gui.UpgradeContainerType;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogicContainer;
import net.p3pp3rf1y.sophisticatedcore.util.NBTHelper;

/**
 * Server-side settings container. Redox upgrades only expose the filter plus up to three switches, so this is the
 * host for Sophisticated Core's own {@link FilterLogicContainer} (filter UI reused as-is, sprites and all) and for
 * the same "mutate locally, then tell the server" round trip the base mod's {@code ToolSwapperUpgradeContainer}
 * uses.
 *
 * <p>Which switches exist is decided by the upgrade item's capabilities, so this one class serves all five
 * upgrades.
 */
public class RedoxUpgradeContainer extends UpgradeContainerBase<RedoxUpgradeWrapper, RedoxUpgradeContainer> {
	private static final String DATA_DEWAX_ALLOWED = "dewaxAllowed";
	private static final String DATA_SHAPE_MATCH = "shapeMatch";
	private static final String DATA_STEPS = "steps";

	private final FilterLogicContainer<FilterLogic> filterLogicContainer;

	public RedoxUpgradeContainer(Player player, int containerId, RedoxUpgradeWrapper wrapper,
			UpgradeContainerType<RedoxUpgradeWrapper, RedoxUpgradeContainer> type) {
		super(player, containerId, wrapper, type);
		filterLogicContainer = new FilterLogicContainer<>(supplyFromWrapper(RedoxUpgradeWrapper::getFilterLogic), this, slots::add);
	}

	public FilterLogicContainer<FilterLogic> getFilterLogicContainer() {
		return filterLogicContainer;
	}

	/** Filter slots per row comes from the upgrade's own tier config, exactly like the base mod's upgrades do. */
	public int getFilterSlotsInRow() {
		return upgradeWrapper.getRedoxItem().getFilterSlotsInRow();
	}

	// ------------------------------------------------------------------ capabilities (drive which switches the tab builds)

	public boolean supportsDewax() {
		return upgradeWrapper.getRedoxItem().supportsDewax();
	}

	public boolean supportsShapeMatch() {
		return upgradeWrapper.getRedoxItem().supportsShapeMatch();
	}

	public boolean supportsSteps() {
		return upgradeWrapper.getRedoxItem().supportsSteps();
	}

	// ------------------------------------------------------------------ settings

	public boolean isDewaxAllowed() {
		return upgradeWrapper.isDewaxAllowed();
	}

	public void setDewaxAllowed(boolean dewaxAllowed) {
		upgradeWrapper.setDewaxAllowed(dewaxAllowed);
		sendBooleanToServer(DATA_DEWAX_ALLOWED, dewaxAllowed);
		// Turning dewaxing off can pull the level down from Ⅳ to Ⅲ, and the button on the client has to follow.
		syncSteps();
	}

	public boolean isShapeMatch() {
		return upgradeWrapper.isShapeMatch();
	}

	public void setShapeMatch(boolean shapeMatch) {
		upgradeWrapper.setShapeMatch(shapeMatch);
		sendBooleanToServer(DATA_SHAPE_MATCH, shapeMatch);
	}

	public int getSteps() {
		return upgradeWrapper.getSteps();
	}

	public void cycleSteps() {
		upgradeWrapper.cycleSteps();
		syncSteps();
	}

	private void syncSteps() {
		int steps = upgradeWrapper.getSteps();
		sendDataToServer(() -> NBTHelper.putInt(new CompoundTag(), DATA_STEPS, steps));
	}

	@Override
	public void handlePacket(CompoundTag data) {
		NBTHelper.getBoolean(data, DATA_DEWAX_ALLOWED).ifPresent(this::setDewaxAllowed);
		NBTHelper.getBoolean(data, DATA_SHAPE_MATCH).ifPresent(this::setShapeMatch);
		NBTHelper.getInt(data, DATA_STEPS).ifPresent(upgradeWrapper::setSteps);
		filterLogicContainer.handlePacket(data);
	}
}
