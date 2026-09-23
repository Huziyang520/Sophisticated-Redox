package com.huziyang520.sophisticatedredox.upgrade.items;

import com.huziyang520.sophisticatedredox.core.RedoxMode;
import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeItem;
import com.huziyang520.sophisticatedredox.upgrade.RedoxUpgradeWrapper;
import net.minecraft.world.item.Item;
import net.p3pp3rf1y.sophisticatedcore.upgrades.UpgradeType;

/**
 * Scrapes copper blocks by up to Ⅲ oxidation stages at once - the level button in its settings picks the exact
 * number, and Ⅲ (the default) means "all the way back to the unaffected stage".
 *
 * <p>This is the only upgrade whose level can reach Ⅳ, and only while "allow dewaxing" is on: Ⅳ is one step to
 * strip the wax plus three reduction steps, i.e. "fully unaffected and unwaxed". Switching dewaxing back off
 * therefore drops a Ⅳ setting back to Ⅲ.
 */
public class AdvancedReductionUpgradeItem extends RedoxUpgradeItem {
	public static final UpgradeType<RedoxUpgradeWrapper> TYPE = new UpgradeType<>(RedoxUpgradeWrapper::new);

	public AdvancedReductionUpgradeItem(Item.Properties properties) {
		super(RedoxMode.REDUCE, true, new Capabilities(true, true, true), properties);
	}

	@Override
	public UpgradeType<RedoxUpgradeWrapper> getType() {
		return TYPE;
	}
}
