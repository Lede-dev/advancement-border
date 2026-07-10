package net.ledestudios.advancementborder.system;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import net.ledestudios.advancementborder.data.BorderRuleData;
import net.ledestudios.advancementborder.data.DimensionKind;
import net.ledestudios.advancementborder.data.DimensionProjectionData;

class DimensionProjectionSystemTest {
	@Test
	void projectsSharedSizeAndConfiguredCenters() {
		List<DimensionProjectionData> projections = DimensionProjectionSystem.project(
				4.5D,
				-2.5D,
				2.0D,
				new BorderRuleData(1, 1, 100, 0)
		);

		assertEquals(List.of(DimensionKind.OVERWORLD, DimensionKind.NETHER, DimensionKind.END), projections.stream().map(DimensionProjectionData::dimension).toList());
		assertEquals(4.5D, projections.get(0).centerX());
		assertEquals(4.5D, projections.get(1).centerX());
		assertEquals(100.5D, projections.get(2).centerX());
		assertEquals(0.5D, projections.get(2).centerZ());
		assertEquals(2.0D, projections.get(2).diameter());
	}
}
