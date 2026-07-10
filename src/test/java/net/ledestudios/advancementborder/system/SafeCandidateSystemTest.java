package net.ledestudios.advancementborder.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.ledestudios.advancementborder.system.SafeCandidateSystem.Column;

class SafeCandidateSystemTest {
	@Test
	void squareSpiralCoversEachColumnOnce() {
		List<Column> columns = SafeCandidateSystem.squareSpiral(10, -5, 2);

		assertEquals(25, columns.size());
		assertEquals(25, new HashSet<>(columns).size());
		assertEquals(new Column(10, -5), columns.getFirst());
	}

	@Test
	void rejectsNegativeRadius() {
		assertThrows(IllegalArgumentException.class, () -> SafeCandidateSystem.squareSpiral(0, 0, -1));
	}
}
