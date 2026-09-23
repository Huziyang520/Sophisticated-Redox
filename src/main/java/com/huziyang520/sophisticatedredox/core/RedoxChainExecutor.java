package com.huziyang520.sophisticatedredox.core;

import com.huziyang520.sophisticatedredox.Constants;
import com.huziyang520.sophisticatedredox.config.RedoxConfig;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IUpgradeWrapper;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Runs the whole redox chain for one changed slot, exactly once per change.
 *
 * <p>Ordering rules (see the project plan):
 * <ol>
 *     <li>Redox upgrades are applied in <b>upgrade-slot order</b>, i.e. the order the player installed them in.
 *         So "Oxidation + Reduction" installed in that order cancels out, while the reverse order does nothing -
 *         which is exactly the agreed behaviour (plan decision "方案A").</li>
 *     <li>Waxing upgrades are <b>always moved to the end</b>, whatever slot they sit in. This is what makes
 *         "oxidize then wax" work: once waxed, a block is skipped by all oxidation / reduction upgrades
 *         (vanilla semantics: wax stops weathering).</li>
 * </ol>
 *
 * <p>Each upgrade performs up to {@link IRedoxUpgrade#getSteps()} single steps, stopping early as soon as the
 * block saturates (already fully oxidized / fully unaffected / no wax left to strip) or the filter no longer
 * matches. Only the first redox upgrade of a backpack drives the chain (see the wrapper), so a backpack with N
 * redox upgrades still does exactly one pass.
 */
public final class RedoxChainExecutor {
	private RedoxChainExecutor() {
	}

	/**
	 * Collects the enabled redox upgrades of an upgrade handler, sorted by upgrade slot ascending.
	 *
	 * <p>Deliberately built from {@link UpgradeHandler#getSlotWrappers()} rather than
	 * {@code getWrappersThatImplementFromMainStorage(...)}: the latter iterates a map's values and caches the result
	 * per class, so its order is <b>not</b> the upgrade slot order - and "applied in the order the player installed
	 * them" is a promise this mod makes in its tooltips.
	 */
	public static List<IRedoxUpgrade> orderedUpgrades(UpgradeHandler upgradeHandler) {
		List<Integer> slots = new ArrayList<>(upgradeHandler.getSlotWrappers().keySet());
		Collections.sort(slots);
		List<IRedoxUpgrade> result = new ArrayList<>(slots.size());
		for (int slot : slots) {
			IUpgradeWrapper wrapper = upgradeHandler.getSlotWrappers().get(slot);
			if (wrapper instanceof IRedoxUpgrade redox && wrapper.isEnabled()) {
				result.add(redox);
			}
		}
		return result;
	}

	/**
	 * @param orderedRedoxUpgrades all enabled redox upgrades of the backpack, sorted by upgrade slot ascending
	 *     (see {@link #orderedUpgrades})
	 */
	public static void run(InventoryHandler inventoryHandler, int slot, List<IRedoxUpgrade> orderedRedoxUpgrades) {
		if (orderedRedoxUpgrades.isEmpty()) {
			return;
		}
		for (IRedoxUpgrade upgrade : order(orderedRedoxUpgrades)) {
			runUpgrade(inventoryHandler, slot, upgrade);
		}
	}

	/** Non-waxing upgrades first (in their given order), then every waxing upgrade. */
	private static List<IRedoxUpgrade> order(List<IRedoxUpgrade> upgrades) {
		List<IRedoxUpgrade> ordered = new ArrayList<>(upgrades.size());
		for (IRedoxUpgrade upgrade : upgrades) {
			if (!upgrade.getRedoxMode().isWaxing()) {
				ordered.add(upgrade);
			}
		}
		for (IRedoxUpgrade upgrade : upgrades) {
			if (upgrade.getRedoxMode().isWaxing()) {
				ordered.add(upgrade);
			}
		}
		return ordered;
	}

	private static void runUpgrade(InventoryHandler inventoryHandler, int slot, IRedoxUpgrade upgrade) {
		RedoxMode mode = upgrade.getRedoxMode();
		boolean dewaxAllowed = upgrade.isDewaxAllowed();
		int steps = Math.max(1, upgrade.getSteps());
		boolean debug = RedoxConfig.SERVER.isDebugLogging();
		for (int step = 0; step < steps; step++) {
			ItemStack current = inventoryHandler.getStackInSlot(slot);
			if (current.isEmpty() || !upgrade.matches(current)) {
				return;
			}
			ItemStack converted = CopperConversionLogic.step(current, mode, dewaxAllowed);
			if (converted == current) {
				// saturated (or waxed without permission) -> same instance, nothing more this upgrade can do
				if (debug) {
					Constants.LOG.info("[redox] slot {}: {} performs no further {} step - stopping at step {}", slot,
							current.getItem(), mode, step);
				}
				return;
			}
			if (!write(inventoryHandler, slot, converted)) {
				return;
			}
			if (debug) {
				Constants.LOG.info("[redox] slot {}: step {}/{} {} -> {} via {}", slot, step + 1, steps,
						current.getItem(), converted.getItem(), upgrade.getClass().getSimpleName());
			}
		}
	}

	/**
	 * Writes the converted stack back through the storage handler.
	 *
	 * <p>{@code setStackInSlot} writes the storage handler directly (the GUI {@code Slot} is not involved), so this
	 * neither re-enters {@code onSlotChange} nor re-enters {@code onAfterInsert}. The capacity is checked first so a
	 * conversion can never spill items on the floor.
	 */
	private static boolean write(InventoryHandler inventoryHandler, int slot, ItemStack converted) {
		ItemResource convertedResource = ItemResource.of(converted);
		long capacity = inventoryHandler.getCapacityAsLong(slot, convertedResource);
		if (capacity > 0 && converted.getCount() > capacity) {
			return false;
		}
		inventoryHandler.setStackInSlot(slot, converted);
		return true;
	}
}
