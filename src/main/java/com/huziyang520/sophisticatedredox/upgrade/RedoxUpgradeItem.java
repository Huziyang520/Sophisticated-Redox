package com.huziyang520.sophisticatedredox.upgrade;

import com.huziyang520.sophisticatedredox.config.RedoxConfig;
import com.huziyang520.sophisticatedredox.core.RedoxMode;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedbackpacks.Config;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeItemBase;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;

import java.util.List;

/**
 * Common base for the five redox upgrades. Everything except the {@link RedoxMode}, the tier and the
 * {@link Capabilities} is shared, so each concrete upgrade is a ~15 line class that only declares those and its own
 * {@link UpgradeType}.
 *
 * <p>Two deliberate "be like the base mod" decisions:
 * <ul>
 *     <li>the per-backpack limit is the base mod's own
 *         {@link Config.Server#maxUpgradesPerStorage}, exactly the instance the base mod hands to its own upgrade
 *         items - so redox upgrades are governed by the same {@code "upgradeRegistryName|maxNumber"} list and
 *         behave identically when it comes to being insertable;</li>
 *     <li>the tier only selects which filter configuration is used, mirroring how the base mod pairs
 *         "Magnet Upgrade" with "Advanced Magnet Upgrade" (advanced gets more slots laid out wider).</li>
 * </ul>
 */
public abstract class RedoxUpgradeItem extends UpgradeItemBase<RedoxUpgradeWrapper> {
	private final RedoxMode mode;
	private final boolean advanced;
	private final Capabilities capabilities;

	protected RedoxUpgradeItem(RedoxMode mode, boolean advanced, Capabilities capabilities, Item.Properties properties) {
		super(Config.SERVER.maxUpgradesPerStorage, properties);
		this.mode = mode;
		this.advanced = advanced;
		this.capabilities = capabilities;
	}

	/**
	 * Which of the three optional settings this upgrade exposes. The settings page builds its button row from this,
	 * so a capability that is off is not merely hidden - the wrapper refuses to honour it as well.
	 */
	public record Capabilities(boolean dewax, boolean shapeMatch, boolean steps) {
		/** Plain one-step upgrades: oxidation, waxing. */
		public static final Capabilities NONE = new Capabilities(false, false, false);
	}

	public RedoxMode getMode() {
		return mode;
	}

	public boolean isAdvanced() {
		return advanced;
	}

	public Capabilities getCapabilities() {
		return capabilities;
	}

	public int getFilterSlotCount() {
		return RedoxConfig.SERVER.filterSlots(advanced);
	}

	public int getFilterSlotsInRow() {
		return RedoxConfig.SERVER.filterSlotsInRow(advanced);
	}

	/** Level a freshly crafted advanced upgrade starts at - Ⅰ for the fixed one-step upgrades. */
	public int defaultSteps() {
		return supportsSteps() ? RedoxConfig.SERVER.defaultSteps() : 1;
	}

	/**
	 * Upper bound of the level setting. Only the advanced reduction upgrade can reach Ⅳ, and only while dewaxing is
	 * allowed - Ⅳ is "one step to strip the wax, then three reduction steps".
	 */
	public int maxSteps(boolean dewaxAllowed) {
		if (!supportsSteps()) {
			return 1;
		}
		return supportsDewax() && dewaxAllowed ? RedoxConfig.SERVER.maxStepsWithDewax() : RedoxConfig.SERVER.maxSteps();
	}

	public boolean supportsDewax() {
		return capabilities.dewax();
	}

	public boolean supportsShapeMatch() {
		return capabilities.shapeMatch();
	}

	public boolean supportsSteps() {
		return capabilities.steps();
	}

	/** Clamps a raw level setting into the range this upgrade currently allows. */
	public int clampSteps(int steps, boolean dewaxAllowed) {
		return Mth.clamp(steps, 1, maxSteps(dewaxAllowed));
	}

	/** Redox upgrades are independent of each other, so there are no conflicts to declare. */
	@Override
	public List<UpgradeConflictDefinition> getUpgradeConflicts() {
		return List.of();
	}

	@Override
	public abstract UpgradeType<RedoxUpgradeWrapper> getType();
}
