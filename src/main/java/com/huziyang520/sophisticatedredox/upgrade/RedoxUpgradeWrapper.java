package com.huziyang520.sophisticatedredox.upgrade;

import com.huziyang520.sophisticatedredox.core.IRedoxUpgrade;
import com.huziyang520.sophisticatedredox.core.RedoxMode;
import com.huziyang520.sophisticatedredox.core.RedoxTickQueue;
import com.huziyang520.sophisticatedredox.init.ModDataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.p3pp3rf1y.sophisticatedcore.api.ISlotChangeResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IFilteredUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.IInsertResponseUpgrade;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeWrapperBase;

import java.util.function.Consumer;

/**
 * The runtime behaviour of every redox upgrade.
 *
 * <p>Like every upgrade of the base mod it is switchable: {@link #canBeDisabled()} is on, the enabled flag lives in
 * the upgrade stack's own data components and Sophisticated Core draws the on/off switch automatically for any
 * upgrade whose wrapper allows it.
 *
 * <h2>Why both response interfaces are needed</h2>
 * There are two completely separate ways a stack can end up in a storage slot, and the base mod notifies upgrades
 * about each of them through a different interface:
 * <ul>
 *     <li><b>programmatic inserts</b> (hoppers, pickup/magnet upgrades, anything calling
 *         {@code InventoryHandler#insert}) -> {@link IInsertResponseUpgrade#onAfterInsert};</li>
 *     <li><b>GUI-driven changes</b> (dragging, shift-clicking, {@code safeInsert})
 *         -> {@link ISlotChangeResponseUpgrade#onSlotChange}, dispatched from
 *         {@code StorageInventorySlot#set} because those paths write the slot with
 *         {@code setStackInSlot} and never call {@code insert}.</li>
 * </ul>
 *
 * <h2>Why the hooks only schedule, never convert</h2>
 * {@link RedoxTickQueue} documents why converting inside these hooks gets rolled back by
 * {@code SlotSuppliedHandler#setChanged()} and produces ghost items. Every hook therefore just schedules
 * {@code (storage, slot)}; the queue deduplicates the entries and runs the chain once per changed slot at the end of
 * the server tick. Because the queue is keyed on the slot rather than on the upgrade, no "which upgrade drives"
 * arbitration is needed here any more.
 */
public class RedoxUpgradeWrapper extends UpgradeWrapperBase<RedoxUpgradeWrapper, RedoxUpgradeItem>
		implements
			IInsertResponseUpgrade,
			ISlotChangeResponseUpgrade,
			IFilteredUpgrade,
			IRedoxUpgrade {
	private final RedoxFilterLogic filterLogic;

	public RedoxUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade, Consumer<ItemStack> upgradeSaveHandler) {
		super(storageWrapper, upgrade, upgradeSaveHandler);
		filterLogic = new RedoxFilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getFilterSlotCount(), this::isShapeMatch);
	}

	@Override
	public RedoxMode getRedoxMode() {
		return upgradeItem.getMode();
	}

	@Override
	public boolean matches(ItemStack stack) {
		return filterLogic.matchesFilter(stack);
	}

	@Override
	public FilterLogic getFilterLogic() {
		return filterLogic;
	}

	/** Unlike e.g. the everlasting upgrade these are meant to be toggled off from the backpack screen. */
	@Override
	public boolean canBeDisabled() {
		return true;
	}

	public RedoxUpgradeItem getRedoxItem() {
		return upgradeItem;
	}

	// ------------------------------------------------------------------ settings

	@Override
	public boolean isDewaxAllowed() {
		return upgradeItem.supportsDewax() && upgrade.getOrDefault(dewaxComponent(), false);
	}

	public void setDewaxAllowed(boolean dewaxAllowed) {
		if (!upgradeItem.supportsDewax()) {
			return;
		}
		upgrade.set(dewaxComponent(), dewaxAllowed);
		// Ⅳ only exists while dewaxing is allowed (one step to strip the wax + three reduction steps), so switching
		// it back off has to pull a Ⅳ setting down to the highest level that is still valid.
		int maxSteps = upgradeItem.maxSteps(dewaxAllowed);
		if (getRawSteps() > maxSteps) {
			upgrade.set(stepsComponent(), maxSteps);
		}
		save();
	}

	@Override
	public boolean isShapeMatch() {
		return upgradeItem.supportsShapeMatch() && upgrade.getOrDefault(shapeMatchComponent(), false);
	}

	public void setShapeMatch(boolean shapeMatch) {
		if (!upgradeItem.supportsShapeMatch()) {
			return;
		}
		upgrade.set(shapeMatchComponent(), shapeMatch);
		save();
	}

	@Override
	public int getSteps() {
		return upgradeItem.clampSteps(getRawSteps(), isDewaxAllowed());
	}

	public void setSteps(int steps) {
		if (!upgradeItem.supportsSteps()) {
			return;
		}
		upgrade.set(stepsComponent(), upgradeItem.clampSteps(steps, isDewaxAllowed()));
		save();
	}

	/** Advances the level button by one, wrapping around at the maximum this upgrade currently allows. */
	public void cycleSteps() {
		int maxSteps = upgradeItem.maxSteps(isDewaxAllowed());
		int next = getSteps() + 1;
		setSteps(next > maxSteps ? 1 : next);
	}

	private int getRawSteps() {
		return upgrade.getOrDefault(stepsComponent(), upgradeItem.defaultSteps());
	}

	private static DataComponentType<Boolean> dewaxComponent() {
		return ModDataComponents.DEWAX_ALLOWED.get();
	}

	private static DataComponentType<Boolean> shapeMatchComponent() {
		return ModDataComponents.SHAPE_MATCH.get();
	}

	private static DataComponentType<Integer> stepsComponent() {
		return ModDataComponents.REDOX_STEPS.get();
	}

	// ------------------------------------------------------------------ scheduling

	@Override
	public void onAfterInsert(InventoryHandler inventoryHandler, int slot, TransactionContext tx) {
		RedoxTickQueue.enqueue(storageWrapper, slot);
	}

	@Override
	public void onSlotChange(InventoryHandler inventoryHandler, int slot) {
		RedoxTickQueue.enqueue(storageWrapper, slot);
	}

	/**
	 * Applies the chain to everything already sitting in the storage, so an upgrade installed into a backpack that
	 * already holds copper blocks starts working immediately instead of silently waiting for a re-insert.
	 *
	 * <p>Called by Sophisticated Core from the server thread only, and only when an upgrade was actually just added.
	 * Every occupied slot is scheduled - the queue deduplicates, and the drain-time re-check drops slots that no
	 * longer have a matching stack.
	 */
	@Override
	public void onAdded() {
		InventoryHandler inventoryHandler = storageWrapper.getInventoryHandler();
		for (int slot = 0; slot < inventoryHandler.size(); slot++) {
			if (!inventoryHandler.getStackInSlot(slot).isEmpty()) {
				RedoxTickQueue.enqueue(storageWrapper, slot);
			}
		}
	}
}
