package com.huziyang520.sophisticatedredox.core;

/**
 * The three redox directions. Everything that differs between the five upgrades other than direction (how many
 * steps, whether dewaxing is allowed, whether shapes are matched) is a per-upgrade setting, so the conversion
 * logic itself stays shared.
 *
 * <p>The old {@code OXIDIZE_MAX} / {@code REDUCE_MAX} modes are gone: "jump to the end of the chain" is now
 * expressed as {@code steps = 3} on the advanced upgrades, which saturates at the same place but lets the player
 * pick Ⅰ/Ⅱ/Ⅲ (and Ⅳ for reduction with dewaxing).
 */
public enum RedoxMode {
	/** Move the block one stage towards fully oxidized (unaffected -> exposed -> weathered -> oxidized). */
	OXIDIZE,
	/** Move the block one stage back towards unaffected, optionally stripping wax first. */
	REDUCE,
	/** Apply honeycomb to lock the block at its current oxidation stage - or strip it when dewaxing is enabled. */
	WAX;

	/** Waxing upgrades always execute last, so that "oxidize then wax" locks the block at the new stage. */
	public boolean isWaxing() {
		return this == WAX;
	}

	/** True for the oxidation direction of the weathering chain. */
	public boolean isOxidizing() {
		return this == OXIDIZE;
	}
}
