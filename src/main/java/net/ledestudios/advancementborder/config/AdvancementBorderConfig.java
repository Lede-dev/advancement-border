package net.ledestudios.advancementborder.config;

import net.ledestudios.advancementborder.data.BorderRuleData;

public record AdvancementBorderConfig(
		int schemaVersion,
		int initialDiameter,
		int growthPerAdvancement,
		EndCenterBlock endCenterBlock
) {
	public static final int CURRENT_SCHEMA_VERSION = 1;
	public static final AdvancementBorderConfig DEFAULT = new AdvancementBorderConfig(
			CURRENT_SCHEMA_VERSION,
			1,
			2,
			new EndCenterBlock(100, 0)
	);

	public AdvancementBorderConfig {
		if (schemaVersion != CURRENT_SCHEMA_VERSION) {
			throw new IllegalArgumentException("지원하지 않는 schemaVersion입니다: " + schemaVersion);
		}
		if (initialDiameter < 1) {
			throw new IllegalArgumentException("initialDiameter는 1 이상이어야 합니다.");
		}
		if (growthPerAdvancement < 1) {
			throw new IllegalArgumentException("growthPerAdvancement는 1 이상이어야 합니다.");
		}
		if (endCenterBlock == null) {
			endCenterBlock = DEFAULT.endCenterBlock();
		}
	}

	public BorderRuleData toRules() {
		return new BorderRuleData(initialDiameter, growthPerAdvancement, endCenterBlock.x(), endCenterBlock.z());
	}

	public record EndCenterBlock(int x, int z) {
	}
}
