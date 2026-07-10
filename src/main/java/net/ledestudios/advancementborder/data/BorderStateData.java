package net.ledestudios.advancementborder.data;

import java.util.UUID;

public record BorderStateData(
		boolean active,
		UUID ownerUuid,
		int anchorX,
		int anchorY,
		int anchorZ,
		int completedCount,
		double appliedDiameter,
		String lastError
) {
	public double overworldCenterX() {
		return anchorX + 0.5D;
	}

	public double overworldCenterZ() {
		return anchorZ + 0.5D;
	}
}
