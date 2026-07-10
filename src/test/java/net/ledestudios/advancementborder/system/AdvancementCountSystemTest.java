package net.ledestudios.advancementborder.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.ledestudios.advancementborder.data.AdvancementCatalogData;
import net.ledestudios.advancementborder.data.PlayerProgressData;

class AdvancementCountSystemTest {
	@Test
	void countsOnlyCompletedEligibleIndices() {
		BitSet eligible = new BitSet();
		eligible.set(1);
		eligible.set(2);
		AdvancementCatalogData catalog = new AdvancementCatalogData(
				List.of("root", "minecraft:child", "example:child"),
				Map.of("root", 0, "minecraft:child", 1, "example:child", 2),
				eligible
		);

		BitSet completed = new BitSet();
		completed.set(0);
		completed.set(2);

		assertEquals(1, AdvancementCountSystem.count(catalog, new PlayerProgressData(completed)));
	}
}
