package com.huziyang520.sophisticatedredox.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilteredUpgradeConfig;

/**
 * SERVER config, deliberately shaped like {@code sophisticatedbackpacks}'s own magnet upgrade config: one
 * {@link FilteredUpgradeConfig} per tier, supplying {@code filterSlots} and {@code slotsInRow}.
 *
 * <p>Basic tier mirrors {@code magnetUpgrade} (9 slots, 3 per row); the advanced tier mirrors
 * {@code advancedMagnetUpgrade} (16 slots, 4 per row).
 *
 * <p>The per-backpack upgrade limit is deliberately NOT re-implemented here: the items hand
 * {@code sophisticatedbackpacks}' own {@code maxUpgradesPerStorage} to {@code UpgradeItemBase}, so redox upgrades
 * are registered in the very same {@code "upgradeRegistryName|maxNumber"} list as every base-mod upgrade
 * (unlisted types fall back to unlimited, which is also the base mod's own convention).
 */
public final class RedoxConfig {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final Server SERVER = new Server(BUILDER);
	public static final ModConfigSpec SPEC = BUILDER.build();

	private RedoxConfig() {
	}

	public static final class Server {
		public final FilteredUpgradeConfig redoxUpgrade;
		public final FilteredUpgradeConfig advancedRedoxUpgrade;

		public final ModConfigSpec.BooleanValue debugLogging;
		public final ModConfigSpec.IntValue defaultSteps;
		public final ModConfigSpec.IntValue maxSteps;
		public final ModConfigSpec.IntValue maxStepsWithDewax;

		private Server(ModConfigSpec.Builder builder) {
			redoxUpgrade = new FilteredUpgradeConfig(builder, "Redox Upgrade", "redoxUpgrade", 9, 3);
			advancedRedoxUpgrade = new FilteredUpgradeConfig(builder, "Advanced Redox Upgrade", "advancedRedoxUpgrade", 16, 4);

			builder.comment("Diagnostics").push("diagnostics");
			debugLogging = builder
					.comment("Logs every conversion step of the redox chain (slot, upgrade, items, saturation) to the game log.",
							"Off by default - only turn this on while investigating why an upgrade does not seem to do anything.")
					.define("debugLogging", false);
			builder.pop();

			builder.comment("Step counts of the advanced upgrades",
							"A step is one single conversion: one oxidation stage, one reduction stage, or stripping the wax.",
							"The level button in the upgrade's settings cycles between these values, up to the maximum for that upgrade.")
					.push("steps");
			defaultSteps = builder.comment("Level a freshly crafted advanced upgrade starts at (Ⅲ by default).")
					.defineInRange("defaultSteps", 3, 1, 4);
			maxSteps = builder.comment("Upper bound when waxing / dewaxing is not involved (Ⅲ by default).")
					.defineInRange("maxSteps", 3, 1, 4);
			maxStepsWithDewax = builder.comment("Upper bound for an advanced reduction upgrade with dewaxing allowed (Ⅳ by default:",
							"one step to strip the wax, then up to three reduction steps).")
					.defineInRange("maxStepsWithDewax", 4, 1, 4);
			builder.pop();
		}

		public int filterSlots(boolean advanced) {
			if (!SPEC.isLoaded()) {
				return advanced ? 16 : 9;
			}
			return advanced ? advancedRedoxUpgrade.filterSlots.get() : redoxUpgrade.filterSlots.get();
		}

		public int filterSlotsInRow(boolean advanced) {
			if (!SPEC.isLoaded()) {
				return advanced ? 4 : 3;
			}
			return advanced ? advancedRedoxUpgrade.slotsInRow.get() : redoxUpgrade.slotsInRow.get();
		}

		public boolean isDebugLogging() {
			return SPEC.isLoaded() && debugLogging.get();
		}

		public int defaultSteps() {
			return SPEC.isLoaded() ? defaultSteps.get() : 3;
		}

		public int maxSteps() {
			return SPEC.isLoaded() ? maxSteps.get() : 3;
		}

		public int maxStepsWithDewax() {
			return SPEC.isLoaded() ? maxStepsWithDewax.get() : 4;
		}
	}
}
