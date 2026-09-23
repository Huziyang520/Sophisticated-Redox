package com.huziyang520.sophisticatedredox.upgrade.items;

import com.huziyang520.sophisticatedredox.core.RedoxMode;
import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeItem;
import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeWrapper;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;

/** Oxidizes copper blocks one stage further when they are inserted into the backpack. */
public class OxidationUpgradeItem extends RedoxUpgradeItem {
	public static final UpgradeType<RedoxUpgradeWrapper> TYPE = new UpgradeType<>(RedoxUpgradeWrapper::new);

	public OxidationUpgradeItem(Item.Properties properties) {
		super(RedoxMode.OXIDIZE, false, Capabilities.NONE, properties);
	}

	@Override
	public UpgradeType<RedoxUpgradeWrapper> getType() {
		return TYPE;
	}
}
