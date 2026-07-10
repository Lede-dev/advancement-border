package net.ledestudios.advancementborder.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AdvancementBorderConfigTest {
	@Test
	void validatesMinimumGrowth() {
		assertThrows(
				IllegalArgumentException.class,
				() -> new AdvancementBorderConfig(1, 1, 0, new AdvancementBorderConfig.EndCenterBlock(100, 0))
		);
	}

	@Test
	void convertsToPureRuleData() {
		AdvancementBorderConfig config = new AdvancementBorderConfig(
				1,
				3,
				1,
				new AdvancementBorderConfig.EndCenterBlock(42, -9)
		);

		assertEquals(3, config.toRules().initialDiameter());
		assertEquals(1, config.toRules().growthPerAdvancement());
		assertEquals(42, config.toRules().endCenterBlockX());
		assertEquals(-9, config.toRules().endCenterBlockZ());
	}
}
