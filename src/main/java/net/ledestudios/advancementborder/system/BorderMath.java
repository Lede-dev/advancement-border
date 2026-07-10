package net.ledestudios.advancementborder.system;

import net.ledestudios.advancementborder.data.BorderRuleData;

public final class BorderMath {
	public static final long VANILLA_MAX_DIAMETER = 59_999_968L;

	private BorderMath() {
	}

	public static long calculateDiameter(BorderRuleData rules, int completedCount) {
		if (completedCount < 0) {
			throw new IllegalArgumentException("completedCount must not be negative");
		}

		try {
			long growth = Math.multiplyExact((long) rules.growthPerAdvancement(), completedCount);
			return Math.min(VANILLA_MAX_DIAMETER, Math.addExact(rules.initialDiameter(), growth));
		} catch (ArithmeticException ignored) {
			return VANILLA_MAX_DIAMETER;
		}
	}
}
