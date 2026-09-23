package com.huziyang520.sophisticatedredox.core;

import com.huziyang520.sophisticatedredox.config.RedoxConfig;
import com.huziyang520.sophisticatedredox.Constants;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.p3pp3rf1y.sophisticatedcore.api.IStorageWrapper;
import net.p3pp3rf1y.sophisticatedcore.inventory.InventoryHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Defers the redox chain to the end of the server tick, and this is not an optimisation - it is what keeps the
 * conversion from being rolled back (the "ghost item" bug).
 *
 * <h2>Why the chain cannot run inside the hook</h2>
 * When a storage GUI is open, its slots are Sophisticated Core's {@code SlotSuppliedHandler}. Two facts about that
 * class combine badly with a conversion done inside {@code Slot#set}:
 * <ol>
 *     <li>{@code set(stack)} writes the handler and then caches the stack it was given, <b>without</b> refreshing
 *         the {@code lastHandlerStack} marker that {@code getItem()} uses to detect external changes;</li>
 *     <li>{@code setChanged()} compares that cached stack against the handler and <b>writes the cached stack back</b>
 *         whenever they differ.</li>
 * </ol>
 * {@code StorageInventorySlot.set()} dispatches {@code onSlotChange} <em>inside</em> {@code set()}, and the menu code
 * calls {@code slot.setChanged()} immediately after {@code slot.set()} all over
 * {@code StorageContainerMenuBase}. So a conversion performed in the hook leaves the cache holding the
 * pre-conversion item while the handler holds the converted one - and the very next {@code setChanged()} writes the
 * pre-conversion item back, rolling the conversion off. A client that was already told about the converted item is
 * then out of sync with the server: the ghost item.
 *
 * <p>Running the chain at the end of the tick sidesteps the whole window: the next
 * {@code AbstractContainerMenu#broadcastChanges()} starts by calling {@code getItem()}, which notices
 * {@code lastHandlerStack != handlerStack} and refreshes the cache to what we wrote - after which every
 * {@code setChanged()} compares equal and never writes anything back.
 *
 * <p>Deferring also fixes a second, less visible problem: {@code InventoryHandler#insert(int, ...)} invokes
 * {@code onAfterInsert} <b>before</b> the caller commits the transaction, so a purely simulated insert could trigger a
 * conversion. Reading the slot at the end of the tick means reading the settled state - a stack that was never really
 * inserted is simply not there any more and gets skipped by the filter check.
 *
 * <h2>Deduplication</h2>
 * Entries are keyed on {@code (storage, slot)}, so however many redox upgrades react to one change, the chain runs
 * exactly once per changed slot. The per-upgrade "only the lowest slot drives" rule therefore lives here rather than
 * in the wrapper.
 */
public final class RedoxTickQueue {
	private record Entry(IStorageWrapper storage, int slot) {
	}

	private static final Set<Entry> PENDING = Collections.newSetFromMap(new java.util.LinkedHashMap<>());

	private RedoxTickQueue() {
	}

	/**
	 * Schedules the chain for one storage slot. Client-side copies never enqueue - the hooks can be reached on the
	 * client too, and only the server owns the real contents.
	 */
	public static void enqueue(IStorageWrapper storage, int slot) {
		if (Thread.currentThread().getThreadGroup() != SidedThreadGroups.SERVER) {
			return;
		}
		synchronized (PENDING) {
			PENDING.add(new Entry(storage, slot));
		}
	}

	/** Runs every scheduled chain. Fired once per server tick, after all container work is done. */
	public static void onServerTick(ServerTickEvent.Post event) {
		List<Entry> batch;
		synchronized (PENDING) {
			if (PENDING.isEmpty()) {
				return;
			}
			batch = new ArrayList<>(PENDING);
			PENDING.clear();
		}
		boolean debug = RedoxConfig.SERVER.isDebugLogging();
		for (Entry entry : batch) {
			run(entry, debug);
		}
	}

	private static void run(Entry entry, boolean debug) {
		InventoryHandler inventoryHandler = entry.storage().getInventoryHandler();
		if (entry.slot() < 0 || entry.slot() >= inventoryHandler.size()) {
			return;
		}
		// Re-read at drain time: the transaction that produced this entry may have been rolled back, and the
		// upgrade may have been removed or switched off since it was scheduled.
		if (inventoryHandler.getStackInSlot(entry.slot()).isEmpty()) {
			return;
		}
		List<IRedoxUpgrade> upgrades = RedoxChainExecutor.orderedUpgrades(entry.storage().getUpgradeHandler());
		if (upgrades.isEmpty()) {
			return;
		}
		if (debug) {
			Constants.LOG.info("[redox] slot {}: deferred chain over {} upgrade(s)", entry.slot(), upgrades.size());
		}
		RedoxChainExecutor.run(inventoryHandler, entry.slot(), upgrades);
	}

	/** Drops everything scheduled - called when a server stops, so nothing leaks between worlds. */
	public static void clear() {
		synchronized (PENDING) {
			PENDING.clear();
		}
	}
}
