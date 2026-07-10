package net.ledestudios.advancementborder.data;

import java.util.BitSet;

public record PlayerProgressData(BitSet completedMask) {
	public PlayerProgressData {
		completedMask = (BitSet) completedMask.clone();
	}

	@Override
	public BitSet completedMask() {
		return (BitSet) completedMask.clone();
	}
}
