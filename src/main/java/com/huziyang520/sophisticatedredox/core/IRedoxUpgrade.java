package com.huziyang520.sophisticatedredox.core;

import net.minecraft.world.item.ItemStack;

/**
 * Marker + accessor for redox upgrades. Used by {@link RedoxChainExecutor} to collect the redox upgrades of a
 * backpack and to read their per-upgrade settings without knowing which of the five it is.
 *
 * <p>The settings live on the upgrade item stack itself (see {@code ModDataComponents}), so two upgrades of the
 * same type inside one backpack can be configured independently.
 */
public interface IRedoxUpgrade {
	RedoxMode getRedoxMode();

	/** Whether this upgrade's own whitelist/blacklist lets the stack through (including shape matching if on). */
	boolean matches(ItemStack stack);

	/** How many single conversion steps this upgrade performs per insertion, clamped to its own maximum. */
	int getSteps();

	/** Whether a reduction upgrade may strip wax (as the first step) instead of skipping waxed blocks. */
	boolean isDewaxAllowed();

	/** Whether the filter compares copper block "shapes" instead of exact items. */
	boolean isShapeMatch();
}
