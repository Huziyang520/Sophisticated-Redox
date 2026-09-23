package com.huziyang520.sophisticatedredox.upgrade.items;

import com.huziyang520.sophisticatedredox.core.RedoxMode;
import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeItem;
import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeWrapper;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;

/**
 * Scrapes one oxidation stage off copper blocks when they are inserted (oxidized -> weathered -> exposed ->
 * unaffected). Waxed blocks are skipped unless "allow dewaxing" is switched on, in which case that one step
 * strips the wax instead.
 */
public class ReductionUpgradeItem extends RedoxUpgradeItem {
	public static final UpgradeType<RedoxUpgradeWrapper> TYPE = new UpgradeType<>(RedoxUpgradeWrapper::new);

	public ReductionUpgradeItem(Item.Properties properties) {
		super(RedoxMode.REDUCE, false, new Capabilities(true, false, false), properties);
	}

	@Override
	public UpgradeType<RedoxUpgradeWrapper> getType() {
		return TYPE;
	}
}
