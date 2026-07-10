package net.ledestudios.advancementborder.system;

import java.util.List;

import net.ledestudios.advancementborder.data.BorderRuleData;
import net.ledestudios.advancementborder.data.DimensionKind;
import net.ledestudios.advancementborder.data.DimensionProjectionData;

public final class DimensionProjectionSystem {
	private DimensionProjectionSystem() {
	}

	public static List<DimensionProjectionData> project(
			double overworldCenterX,
			double overworldCenterZ,
			double diameter,
			BorderRuleData rules
	) {
		return List.of(
				new DimensionProjectionData(DimensionKind.OVERWORLD, overworldCenterX, overworldCenterZ, diameter),
				new DimensionProjectionData(DimensionKind.NETHER, overworldCenterX, overworldCenterZ, diameter),
				new DimensionProjectionData(DimensionKind.END, rules.endCenterBlockX() + 0.5D, rules.endCenterBlockZ() + 0.5D, diameter)
		);
	}
}
