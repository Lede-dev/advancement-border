package net.ledestudios.advancementborder.system;

import java.util.BitSet;

import net.ledestudios.advancementborder.data.AdvancementCatalogData;
import net.ledestudios.advancementborder.data.PlayerProgressData;

public final class AdvancementCountSystem {
	private AdvancementCountSystem() {
	}

	public static int count(AdvancementCatalogData catalog, PlayerProgressData progress) {
		BitSet completedEligible = catalog.eligibleMask();
		completedEligible.and(progress.completedMask());
		return completedEligible.cardinality();
	}
}
