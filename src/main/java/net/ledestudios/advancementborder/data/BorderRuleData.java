package net.ledestudios.advancementborder.data;

public record BorderRuleData(
		int initialDiameter,
		int growthPerAdvancement,
		int endCenterBlockX,
		int endCenterBlockZ
) {
	public static final BorderRuleData DEFAULT = new BorderRuleData(1, 2, 100, 0);

	public BorderRuleData {
		if (initialDiameter < 1) {
			throw new IllegalArgumentException("initialDiameter must be at least 1");
		}
		if (growthPerAdvancement < 1) {
			throw new IllegalArgumentException("growthPerAdvancement must be at least 1");
		}
	}
}
