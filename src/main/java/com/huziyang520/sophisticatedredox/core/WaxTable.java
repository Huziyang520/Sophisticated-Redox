package com.huziyang520.sophisticatedredox.core;

import com.google.common.collect.BiMap;
import com.huziyang520.sophisticatedredox.Constants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The wax / unwax lookup, in <b>both directions</b>, with three independent sources.
 *
 * <p>26.2 moved the wax mapping out of the plain vanilla map that used to exist: {@code HoneycombItem.getWaxed}
 * now reads a NeoForge <b>data map</b> ({@code neoforge:waxables}, shipped inside the NeoForge jar as
 * {@code data/neoforge/data_maps/block/waxables.json}) and only falls back to the deprecated static
 * {@code HoneycombItem.WAXABLES} BiMap. A data map is attached while datapacks load, i.e. it is <em>not</em>
 * guaranteed to be readable at every point in the lifecycle, and reading a single-source lookup early silently
 * yields "no copper block is waxable" - which is exactly the failure mode that made the waxing upgrade look
 * completely dead while oxidation kept working.
 *
 * <p>So this table merges three sources and is usable as long as <em>any</em> of them answers:
 * <ol>
 *     <li>the NeoForge data map {@code neoforge:waxables} (authoritative, covers modded copper too);</li>
 *     <li>vanilla's own {@code WAXABLES} / {@code WAX_OFF_BY_BLOCK} BiMaps (available immediately, no datapack);</li>
 *     <li>a {@code minecraft:} namespace only prefix probe ({@code waxed_<path>}), accepted only when the
 *         reverse lookup of the candidate comes back to the block we started from - so a wrong guess can never
 *         be committed.</li>
 * </ol>
 *
 * <p>Lookups are O(1) {@link HashMap} reads after a single lazy build, and the build is not cached when it yields
 * nothing, so a too-early first call can never poison the result for the rest of the session.
 */
public final class WaxTable {
	@Nullable
	private static Map<Block, Block> waxOn;
	@Nullable
	private static Map<Block, Block> waxOff;

	private WaxTable() {
	}

	/** @return the waxed counterpart of an unwaxed copper block, or {@code null} when there is none. */
	@Nullable
	public static Block waxed(Block block) {
		ensureBuilt();
		Block result = waxOn.get(block);
		if (result == null) {
			// Last-resort per-query probe: the vanilla waxed ids are exactly "waxed_" + the unwaxed path, so this
			// answers for the whole vanilla copper family even if every table source came up empty.
			result = probe(block, "waxed_", "");
		}
		return result;
	}

	/** @return the unwaxed counterpart of a waxed copper block, or {@code null} when the block is not waxed. */
	@Nullable
	public static Block unwaxed(Block block) {
		ensureBuilt();
		Block result = waxOff.get(block);
		if (result == null) {
			result = probe(block, "", "waxed_");
		}
		return result;
	}

	/** True for a waxed copper block - honeycomb already applied, so it no longer weathers. */
	public static boolean isWaxed(Block block) {
		return unwaxed(block) != null;
	}

	/**
	 * Looks a pair up by id prefix, independent of any table: {@code strip} is removed from the block's own path and
	 * {@code add} is prepended to it. Only ever answers for the {@code minecraft:} namespace, so a modded block can
	 * never be matched by accident.
	 */
	@Nullable
	private static Block probe(Block block, String add, String strip) {
		Identifier id = BuiltInRegistries.BLOCK.getKey(block);
		if (id == null || !Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace())) {
			return null;
		}
		String path = id.getPath();
		if (!strip.isEmpty()) {
			if (!path.startsWith(strip)) {
				return null;
			}
			path = path.substring(strip.length());
		} else if (path.startsWith("waxed_")) {
			return null;
		}
		Block candidate = BuiltInRegistries.BLOCK.getOptional(Identifier.fromNamespaceAndPath(id.getNamespace(), add + path)).orElse(null);
		return candidate != null && candidate != block ? candidate : null;
	}

	/**
	 * Drops the cached table so the next lookup rebuilds it. Called when NeoForge finishes loading datapacks,
	 * because the data map source is only populated from that point on.
	 */
	public static synchronized void invalidate() {
		waxOn = null;
		waxOff = null;
	}

	@SuppressWarnings("deprecation") // the vanilla BiMaps are deprecated but stay the datapack-independent fallback
	private static synchronized void ensureBuilt() {
		if (waxOn != null) {
			return;
		}
		Map<Block, Block> on = new HashMap<>();
		Map<Block, Block> off = new HashMap<>();
		int fromDataMap = 0;
		int fromVanilla = 0;
		int fromPrefix = 0;

		// 1) NeoForge data map - the authoritative source in 26.2, and the only one that knows about modded copper.
		//    Wrapped per block: one unexpected holder must not be able to kill the whole build.
		for (Block block : BuiltInRegistries.BLOCK) {
			try {
				Waxable waxable = BuiltInRegistries.BLOCK.wrapAsHolder(block).getData(NeoForgeDataMaps.WAXABLES);
				if (waxable == null) {
					continue;
				}
				Block target = waxable.waxed();
				if (target != null && target != block && isRegistered(target) && on.put(block, target) == null) {
					fromDataMap++;
				}
			} catch (Throwable t) {
				Constants.LOG.warn("Redox: wax data map lookup failed for {}: {}", BuiltInRegistries.BLOCK.getKey(block), t.toString());
			}
		}

		// 2) Vanilla's own BiMaps. These are plain static suppliers (no datapack involved), so they are the
		//    safety net for a first call that happens before datapacks are attached.
		BiMap<Block, Block> vanillaWaxOn = readVanillaMap("WAXABLES", () -> HoneycombItem.WAXABLES.get());
		if (vanillaWaxOn != null) {
			for (Map.Entry<Block, Block> entry : vanillaWaxOn.entrySet()) {
				if (on.putIfAbsent(entry.getKey(), entry.getValue()) == null) {
					fromVanilla++;
				}
			}
		}
		BiMap<Block, Block> vanillaWaxOff = readVanillaMap("WAX_OFF_BY_BLOCK", () -> HoneycombItem.WAX_OFF_BY_BLOCK.get());
		if (vanillaWaxOff != null) {
			// Documented as "wax off by block", i.e. waxed -> unwaxed; the reverse direction is filled from waxOn below.
			for (Map.Entry<Block, Block> entry : vanillaWaxOff.entrySet()) {
				off.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}

		// 2b) The prefix probe as a table source as well, so the reverse direction does not depend on the forward one.
		for (Block block : BuiltInRegistries.BLOCK) {
			Identifier id = BuiltInRegistries.BLOCK.getKey(block);
			if (id == null || !Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace()) || !id.getPath().startsWith("waxed_") || off.containsKey(block)) {
				continue;
			}
			Block candidate = BuiltInRegistries.BLOCK.getOptional(Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath().substring("waxed_".length()))).orElse(null);
			if (candidate != null && candidate != block && isRegistered(candidate) && off.put(block, candidate) == null) {
				fromPrefix++;
			}
		}

		// 3) Last resort: minecraft's waxed ids are exactly "waxed_" + the unwaxed path. Only accepted when the
		//    candidate really is registered and the reverse lookup leads back to the block we started from.
		for (Block block : BuiltInRegistries.BLOCK) {
			Identifier id = BuiltInRegistries.BLOCK.getKey(block);
			if (id == null || !Identifier.DEFAULT_NAMESPACE.equals(id.getNamespace()) || id.getPath().startsWith("waxed_") || on.containsKey(block)) {
				continue;
			}
			Block candidate = BuiltInRegistries.BLOCK.getOptional(Identifier.fromNamespaceAndPath(id.getNamespace(), "waxed_" + id.getPath())).orElse(null);
			if (candidate != null && candidate != block && isRegistered(candidate) && on.put(block, candidate) == null) {
				fromPrefix++;
			}
		}

		// The forward table is authoritative; the strict inverse is merged into it, so a lookup works as long as
		// either direction had data.
		for (Map.Entry<Block, Block> entry : on.entrySet()) {
			off.putIfAbsent(entry.getValue(), entry.getKey());
		}
		// Drop anything that is no longer a registered block, so stale entries can never be handed out.
		off.entrySet().removeIf(entry -> !isRegistered(entry.getKey()) || !isRegistered(entry.getValue()));
		on.entrySet().removeIf(entry -> !isRegistered(entry.getKey()) || !isRegistered(entry.getValue()));

		if (on.isEmpty() || off.isEmpty()) {
			// Do NOT cache an empty result: the next call retries, so an early/cold first call self-heals instead
			// of disabling waxing for the whole session.
			Constants.LOG.error("Redox wax table came up empty (dataMap={}, vanilla={}, prefix={}) - waxing/dewaxing cannot work. "
					+ "This means none of the three lookup sources answered; please report this with the mod list.", fromDataMap, fromVanilla, fromPrefix);
			waxOn = new HashMap<>();
			waxOff = new HashMap<>();
			return;
		}

		Constants.LOG.info("Redox wax table built: {} wax-on pairs, {} wax-off pairs (dataMap={}, vanilla={}, prefix={})",
				on.size(), off.size(), fromDataMap, fromVanilla, fromPrefix);
		waxOn = on;
		waxOff = off;
	}

	/**
	 * Reads one of vanilla's own wax BiMaps.
	 *
	 * <p>Both are marked deprecated in 26.2 ("replaced by the neoforge:waxables data map") but are still populated
	 * and still the only source that does not depend on datapacks having been loaded - which is exactly the safety
	 * net this table needs.
	 */
	@SuppressWarnings("deprecation") // the deprecation is the reason this fallback exists
	@Nullable
	private static BiMap<Block, Block> readVanillaMap(String name, java.util.function.Supplier<BiMap<Block, Block>> supplier) {
		try {
			BiMap<Block, Block> map = supplier.get();
			return map == null || map.isEmpty() ? null : map;
		} catch (Throwable t) {
			// Deprecated in 26.2 - if a future version removes it, the other two sources still carry the table.
			Constants.LOG.warn("Redox could not read vanilla HoneycombItem.{}: {}", name, t.toString());
			return null;
		}
	}

	private static boolean isRegistered(Block block) {
		return BuiltInRegistries.BLOCK.getKey(block) != null;
	}
}
