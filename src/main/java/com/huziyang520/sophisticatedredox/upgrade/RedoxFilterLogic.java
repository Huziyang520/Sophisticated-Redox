package com.huziyang520.sophisticatedredox.upgrade;

import com.huziyang520.sophisticatedredox.core.CopperConversionLogic;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.init.ModCoreDataComponents;
import net.p3pp3rf1y.sophisticatedcore.upgrades.FilterLogic;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * Sophisticated Core's own {@link FilterLogic}, extended with the optional "shape match" comparison of the advanced
 * upgrades.
 *
 * <p>Extending {@code stackMatchesFilter} rather than reimplementing the filter is what keeps the semantics exact:
 * the base class decides whitelist vs blacklist, empty-list behaviour, primary match and component/durability
 * matching, and simply asks this method "does this entry match that stack?". Overriding it therefore upgrades
 * <b>every</b> branch at once - an entry matches by shape in whitelist mode, and blocks by shape in blacklist mode,
 * with no duplicated logic.
 */
public class RedoxFilterLogic extends FilterLogic {
	private final BooleanSupplier shapeMatch;

	public RedoxFilterLogic(ItemStack upgrade, Consumer<ItemStack> saveHandler, int filterSlotCount, BooleanSupplier shapeMatch) {
		// Any copper family block may go into a filter slot. Deliberately more permissive than "convertible right
		// now": with shape match on, a player picks one representative per shape, and that representative may well
		// be a block that is already at the target stage, or waxed. Non-block items are still rejected.
		super(upgrade, saveHandler, filterSlotCount, CopperConversionLogic::isFilterable, ModCoreDataComponents.FILTER_ATTRIBUTES);
		this.shapeMatch = shapeMatch;
	}

	@Override
	public boolean stackMatchesFilter(ItemStack filter, Item item, int damageValue, boolean isEmpty, DataComponentMap components) {
		if (shapeMatch.getAsBoolean() && CopperConversionLogic.sameShape(filter.getItem(), item)) {
			return true;
		}
		return super.stackMatchesFilter(filter, item, damageValue, isEmpty, components);
	}
}
