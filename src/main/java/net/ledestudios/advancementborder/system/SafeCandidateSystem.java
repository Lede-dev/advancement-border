package net.ledestudios.advancementborder.system;

import java.util.ArrayList;
import java.util.List;

public final class SafeCandidateSystem {
	private SafeCandidateSystem() {
	}

	public static List<Column> squareSpiral(int originX, int originZ, int radius) {
		if (radius < 0) {
			throw new IllegalArgumentException("radius must not be negative");
		}

		List<Column> result = new ArrayList<>((radius * 2 + 1) * (radius * 2 + 1));
		result.add(new Column(originX, originZ));

		for (int ring = 1; ring <= radius; ring++) {
			for (int x = -ring; x <= ring; x++) {
				result.add(new Column(originX + x, originZ - ring));
				result.add(new Column(originX + x, originZ + ring));
			}
			for (int z = -ring + 1; z < ring; z++) {
				result.add(new Column(originX - ring, originZ + z));
				result.add(new Column(originX + ring, originZ + z));
			}
		}

		return List.copyOf(result);
	}

	public record Column(int x, int z) {
	}
}
