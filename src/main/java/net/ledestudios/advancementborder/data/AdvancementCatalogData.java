package net.ledestudios.advancementborder.data;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

public record AdvancementCatalogData(List<String> ids, Map<String, Integer> indexById, BitSet eligibleMask) {
	public AdvancementCatalogData {
		ids = List.copyOf(ids);
		indexById = Map.copyOf(indexById);
		eligibleMask = (BitSet) eligibleMask.clone();
	}

	@Override
	public BitSet eligibleMask() {
		return (BitSet) eligibleMask.clone();
	}
}
