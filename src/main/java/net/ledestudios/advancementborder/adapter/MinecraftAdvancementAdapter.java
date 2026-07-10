package net.ledestudios.advancementborder.adapter;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ledestudios.advancementborder.data.AdvancementCatalogData;
import net.ledestudios.advancementborder.data.PlayerProgressData;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class MinecraftAdvancementAdapter {
	private MinecraftAdvancementAdapter() {
	}

	public static AdvancementCatalogData buildCatalog(MinecraftServer server) {
		List<AdvancementHolder> holders = new ArrayList<>(server.getAdvancements().getAllAdvancements());
		holders.sort((left, right) -> left.id().compareTo(right.id()));

		List<String> ids = new ArrayList<>(holders.size());
		Map<String, Integer> indexById = new HashMap<>(holders.size());
		BitSet eligible = new BitSet(holders.size());

		for (int index = 0; index < holders.size(); index++) {
			AdvancementHolder holder = holders.get(index);
			String id = holder.id().toString();
			ids.add(id);
			indexById.put(id, index);

			if (holder.value().display().isPresent() && !holder.value().isRoot()) {
				eligible.set(index);
			}
		}

		return new AdvancementCatalogData(ids, indexById, eligible);
	}

	public static PlayerProgressData snapshotProgress(ServerPlayer player, AdvancementCatalogData catalog) {
		BitSet completed = new BitSet(catalog.ids().size());

		for (int index = 0; index < catalog.ids().size(); index++) {
			Identifier id = Identifier.parse(catalog.ids().get(index));
			AdvancementHolder holder = player.level().getServer().getAdvancements().get(id);
			if (holder != null && player.getAdvancements().getOrStartProgress(holder).isDone()) {
				completed.set(index);
			}
		}

		return new PlayerProgressData(completed);
	}
}
