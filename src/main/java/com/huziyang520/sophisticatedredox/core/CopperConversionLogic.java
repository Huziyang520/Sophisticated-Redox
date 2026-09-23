package com.huziyang520.sophisticatedredox.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;

/**
 * The single entry point for copper block conversion - one <b>single-step</b> conversion at a time.
 *
 * <p>Deliberately does NOT hardcode a table of copper blocks: the whole vanilla copper family (blocks, cut copper
 * and its stairs/slabs, chiseled copper, grates, bulbs, doors, trapdoors, bars, chains, lanterns, chests, golem
 * statues, lightning rods, ...) is read from the very same lookup tables vanilla uses:
 * <ul>
 *     <li>{@link WeatheringCopper#getNext(Block)} / {@link WeatheringCopper#getPrevious(Block)} / {@link WeatheringCopper#getFirst(Block)}</li>
 *     <li>{@link WaxTable} for both the waxing and the dewaxing direction</li>
 * </ul>
 * Any copper block added by vanilla later - or by another mod that registers through vanilla's weathering API -
 * is covered automatically, with zero maintenance. All lookups are O(1) map reads.
 *
 * <p>The multi-step behaviour of the advanced upgrades is <b>not</b> expressed here: {@link #step} always performs
 * exactly one step and the caller decides how many steps to run. That keeps a single conversion table and makes
 * "step N times, stopping when the block saturates" fall out naturally.
 */
public final class CopperConversionLogic {
	private CopperConversionLogic() {
	}

	/** @return the block behind an item stack, or {@code null} when the stack is not a block item. */
	public static Block getBlock(ItemStack stack) {
		return getBlock(stack.getItem());
	}

	/** @return the block behind an item, or {@code null} when it is not a block item. */
	public static Block getBlock(Item item) {
		return item instanceof BlockItem blockItem ? blockItem.getBlock() : null;
	}

	/** True for any block of the copper family, waxed or not. */
	public static boolean isCopperFamily(Block block) {
		return block instanceof WeatheringCopper || WaxTable.isWaxed(block);
	}

	/**
	 * What may be dropped into a filter slot: any block of the copper family. Deliberately a little more permissive
	 * than "convertible right now", because a filter entry may legitimately be a block that is already at the
	 * target stage (or waxed) - notably when {@code shape match} is on and the player picks one representative of a
	 * shape. Non-block items such as cobblestone are still rejected.
	 */
	public static boolean isFilterable(ItemStack stack) {
		Block block = getBlock(stack);
		return block != null && isCopperFamily(block);
	}

	/**
	 * The "shape" of a copper block, used by the shape-match option of the advanced upgrades: strip the wax, then
	 * walk the weathering chain down to its least oxidized member. So every oxidation stage of
	 * {@code copper_block} and all of their waxed variants share one shape key, while {@code cut_copper} - a
	 * different shape - has its own.
	 *
	 * <p>Data driven on purpose: it uses the game's own weathering chain, so new copper blocks are covered for free.
	 * Non-copper blocks are their own shape key, i.e. shape match degrades to an exact match.
	 */
	public static Block shapeKey(Block block) {
		Block unwaxed = WaxTable.unwaxed(block);
		Block base = unwaxed != null ? unwaxed : block;
		return base instanceof WeatheringCopper ? WeatheringCopper.getFirst(base) : base;
	}

	/** True when both stacks are copper blocks of the same shape (see {@link #shapeKey}). */
	public static boolean sameShape(ItemStack a, ItemStack b) {
		return sameShape(a.getItem(), b.getItem());
	}

	/**
	 * True when both items are copper blocks of the same shape (see {@link #shapeKey}).
	 * Non block items - including an empty filter slot's {@code AIR} - never match.
	 */
	public static boolean sameShape(Item a, Item b) {
		Block blockA = getBlock(a);
		Block blockB = getBlock(b);
		return blockA != null && blockB != null && shapeKey(blockA) == shapeKey(blockB);
	}

	/**
	 * Performs exactly one conversion step.
	 *
	 * @param dewaxAllowed whether a reduction upgrade is allowed to strip wax. Ignored by the oxidizing and waxing
	 *     modes. When the block is waxed and this is {@code true}, the step <b>removes the wax instead of reducing</b>
	 *     - dewaxing has to come first, because vanilla semantics make wax stop weathering and a reduction step would
	 *     otherwise be skipped entirely.
	 * @return a new stack with the same count when a conversion happened, or the <em>very same instance</em> when
	 *     nothing could be converted (not copper / already at the limit / waxed without permission). Callers compare
	 *     by identity to detect saturation.
	 */
	public static ItemStack step(ItemStack stack, RedoxMode mode, boolean dewaxAllowed) {
		if (stack.isEmpty()) {
			return stack;
		}
		Block block = getBlock(stack);
		if (block == null) {
			return stack;
		}

		Block target;
		if (mode.isWaxing()) {
			target = WaxTable.waxed(block);
		} else if (WaxTable.isWaxed(block)) {
			target = dewaxAllowed ? WaxTable.unwaxed(block) : null;
		} else {
			target = switch (mode) {
				case OXIDIZE -> WeatheringCopper.getNext(block).orElse(null);
				case REDUCE -> WeatheringCopper.getPrevious(block).orElse(null);
				case WAX -> null;
			};
		}
		if (target == null) {
			return stack;
		}
		Item targetItem = target.asItem();
		if (targetItem == Items.AIR || targetItem == stack.getItem()) {
			return stack;
		}
		// transmuteCopy keeps the count and carries over components that are still valid for the new item
		return stack.transmuteCopy(targetItem, stack.getCount());
	}

	/** Human readable id of a block, for the debug log only. */
	public static String describe(Block block) {
		return String.valueOf(BuiltInRegistries.BLOCK.getKey(block));
	}
}
