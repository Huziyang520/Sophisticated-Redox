package com.huziyang520.sophisticatedredox.upgrade.items;

import com.huziyang520.sophisticatedredox.core.RedoxMode;
import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeItem;
import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeWrapper;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;

/**
 * Oxidizes copper blocks by up to Ⅲ oxidation stages at once - the level button in its settings picks the exact
 * number, and Ⅲ (the default) means "all the way to the fully oxidized stage". It can also match whole block
 * shapes instead of single items, so one filter entry covers every oxidation stage and every waxed variant of
 * that shape.
 */
public class AdvancedOxidationUpgradeItem extends RedoxUpgradeItem {
	public static final UpgradeType<RedoxUpgradeWrapper> TYPE = new UpgradeType<>(RedoxUpgradeWrapper::new);

	public AdvancedOxidationUpgradeItem(Item.Properties properties) {
		super(RedoxMode.OXIDIZE, true, new Capabilities(false, true, true), properties);
	}

	@Override
	public UpgradeType<RedoxUpgradeWrapper> getType() {
		return TYPE;
	}
}
