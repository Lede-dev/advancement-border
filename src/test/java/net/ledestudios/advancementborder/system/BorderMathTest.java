package net.ledestudios.advancementborder.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import net.ledestudios.advancementborder.data.BorderRuleData;

class BorderMathTest {
	@Test
	void defaultGrowthProducesOddSequence() {
		BorderRuleData rules = BorderRuleData.DEFAULT;

		assertEquals(1L, BorderMath.calculateDiameter(rules, 0));
		assertEquals(3L, BorderMath.calculateDiameter(rules, 1));
		assertEquals(5L, BorderMath.calculateDiameter(rules, 2));
		assertEquals(7L, BorderMath.calculateDiameter(rules, 3));
	}

	@Test
	void minimumGrowthProducesEveryIntegerSize() {
		BorderRuleData rules = new BorderRuleData(1, 1, 100, 0);

		assertEquals(1L, BorderMath.calculateDiameter(rules, 0));
		assertEquals(2L, BorderMath.calculateDiameter(rules, 1));
		assertEquals(3L, BorderMath.calculateDiameter(rules, 2));
		assertEquals(4L, BorderMath.calculateDiameter(rules, 3));
	}

	@Test
	void diameterIsClampedToVanillaMaximum() {
		BorderRuleData rules = new BorderRuleData(Integer.MAX_VALUE, Integer.MAX_VALUE, 100, 0);
		assertEquals(BorderMath.VANILLA_MAX_DIAMETER, BorderMath.calculateDiameter(rules, Integer.MAX_VALUE));
	}
}
