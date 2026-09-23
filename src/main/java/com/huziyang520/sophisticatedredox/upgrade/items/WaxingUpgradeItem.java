package com.huziyang520.sophisticatedredox.upgrade.items;

import com.huziyang520.sophisticatedredox.core.RedoxMode;
import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeItem;
import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeWrapper;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;

/**
 * Waxes copper blocks when they are inserted, locking them at their current oxidation stage.
 * Always runs after the oxidation / reduction upgrades of the same backpack.
 */
public class WaxingUpgradeItem extends RedoxUpgradeItem {
	public static final UpgradeType<RedoxUpgradeWrapper> TYPE = new UpgradeType<>(RedoxUpgradeWrapper::new);

	public WaxingUpgradeItem(Item.Properties properties) {
		super(RedoxMode.WAX, false, Capabilities.NONE, properties);
	}

	@Override
	public UpgradeType<RedoxUpgradeWrapper> getType() {
		return TYPE;
	}
}
