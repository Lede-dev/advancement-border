package net.ledestudios.advancementborder.gametest;

import java.util.List;

import net.ledestudios.advancementborder.adapter.MinecraftAdvancementAdapter;
import net.ledestudios.advancementborder.adapter.WorldBorderAdapter;
import net.ledestudios.advancementborder.data.AdvancementCatalogData;
import net.ledestudios.advancementborder.data.BorderRuleData;
import net.ledestudios.advancementborder.data.DimensionProjectionData;
import net.ledestudios.advancementborder.system.AdvancementCountSystem;
import net.ledestudios.advancementborder.system.BorderMath;
import net.ledestudios.advancementborder.system.DimensionProjectionSystem;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.border.BorderStatus;
import net.minecraft.world.level.border.WorldBorder;

import net.fabricmc.fabric.api.gametest.v1.GameTest;

public final class AdvancementBorderGameTests {
	@GameTest
	public void defaultGrowthAndDimensionProjection(GameTestHelper helper) {
		BorderRuleData rules = BorderRuleData.DEFAULT;
		double diameter = BorderMath.calculateDiameter(rules, 2);
		List<DimensionProjectionData> projections = DimensionProjectionSystem.project(10.5D, -4.5D, diameter, rules);

		helper.assertValueEqual(diameter, 5.0D, "two advancements should produce a five block border");
		helper.assertValueEqual(projections.size(), 3, "all three vanilla dimensions should be projected");
		helper.assertValueEqual(projections.get(1).centerX(), 10.5D, "Nether center should remain 1:1");
		helper.assertValueEqual(projections.get(2).centerX(), 100.5D, "End center should use the configured block center");
		helper.succeed();
	}

	@GameTest
	public void oneBlockGrowthKeepsCenterStable(GameTestHelper helper) {
		BorderRuleData rules = new BorderRuleData(1, 1, 100, 0);
		List<DimensionProjectionData> first = DimensionProjectionSystem.project(0.5D, 0.5D, BorderMath.calculateDiameter(rules, 0), rules);
		List<DimensionProjectionData> second = DimensionProjectionSystem.project(0.5D, 0.5D, BorderMath.calculateDiameter(rules, 1), rules);

		helper.assertValueEqual(first.getFirst().diameter(), 1.0D, "initial diameter should be one");
		helper.assertValueEqual(second.getFirst().diameter(), 2.0D, "one advancement should produce a two block border");
		helper.assertValueEqual(first.getFirst().centerX(), second.getFirst().centerX(), "center must not move for even diameters");
		helper.succeed();
	}

	@GameTest
	public void realAdvancementAwardAndRevokeChangeSnapshotCount(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		ServerPlayer player = (ServerPlayer) helper.makeMockServerPlayer(GameType.SURVIVAL);
		AdvancementCatalogData catalog = MinecraftAdvancementAdapter.buildCatalog(server);
		int before = AdvancementCountSystem.count(catalog, MinecraftAdvancementAdapter.snapshotProgress(player, catalog));

		AdvancementHolder target = server.getAdvancements().getAllAdvancements().stream()
				.filter(holder -> holder.value().display().isPresent())
				.filter(holder -> !holder.value().isRoot())
				.filter(holder -> !holder.value().criteria().isEmpty())
				.filter(holder -> !player.getAdvancements().getOrStartProgress(holder).isDone())
				.findFirst()
				.orElseThrow(() -> helper.assertionException("No eligible incomplete advancement was available"));

		try {
			for (String criterion : target.value().criteria().keySet()) {
				player.getAdvancements().award(target, criterion);
			}

			int afterAward = AdvancementCountSystem.count(catalog, MinecraftAdvancementAdapter.snapshotProgress(player, catalog));
			helper.assertValueEqual(afterAward, before + 1, "awarding a displayed child advancement should increase the count");

			String firstCriterion = target.value().criteria().keySet().iterator().next();
			player.getAdvancements().revoke(target, firstCriterion);
			int afterRevoke = AdvancementCountSystem.count(catalog, MinecraftAdvancementAdapter.snapshotProgress(player, catalog));
			helper.assertValueEqual(afterRevoke, before, "revoking a criterion should decrease the completed count");
		} finally {
			for (String criterion : target.value().criteria().keySet()) {
				player.getAdvancements().revoke(target, criterion);
			}
		}

		helper.succeed();
	}

	@GameTest
	public void appliesMatchingBorderSizeToAllVanillaDimensions(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		List<DimensionProjectionData> projections = DimensionProjectionSystem.project(12.5D, -8.5D, 7.0D, BorderRuleData.DEFAULT);
		List<WorldBorderSnapshot> previous = projections.stream()
				.map(projection -> {
					WorldBorder border = WorldBorderAdapter.level(server, projection.dimension()).getWorldBorder();
					return new WorldBorderSnapshot(border, border.getCenterX(), border.getCenterZ(), border.getSize());
				})
				.toList();

		try {
			WorldBorderAdapter.apply(server, projections);
			for (DimensionProjectionData projection : projections) {
				WorldBorder border = WorldBorderAdapter.level(server, projection.dimension()).getWorldBorder();
				helper.assertValueEqual(border.getSize(), 7.0D, projection.dimension() + " border size should match");
				helper.assertValueEqual(border.getCenterX(), projection.centerX(), projection.dimension() + " center X should match");
				helper.assertValueEqual(border.getCenterZ(), projection.centerZ(), projection.dimension() + " center Z should match");
			}
		} finally {
			for (WorldBorderSnapshot snapshot : previous) {
				snapshot.border().setCenter(snapshot.centerX(), snapshot.centerZ());
				snapshot.border().setSize(snapshot.size());
			}
		}

		helper.succeed();
	}

	@GameTest
	public void smoothlyExpandsBordersForThreeSeconds(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		List<DimensionProjectionData> projections = DimensionProjectionSystem.project(12.5D, -8.5D, 7.0D, BorderRuleData.DEFAULT);
		List<WorldBorderSnapshot> previous = projections.stream()
				.map(projection -> {
					WorldBorder border = WorldBorderAdapter.level(server, projection.dimension()).getWorldBorder();
					return new WorldBorderSnapshot(border, border.getCenterX(), border.getCenterZ(), border.getSize());
				})
				.toList();

		try {
			for (WorldBorderSnapshot snapshot : previous) {
				snapshot.border().setSize(1.0D);
			}

			WorldBorderAdapter.apply(server, projections);
			for (DimensionProjectionData projection : projections) {
				WorldBorder border = WorldBorderAdapter.level(server, projection.dimension()).getWorldBorder();
				helper.assertValueEqual(border.getStatus(), BorderStatus.GROWING, projection.dimension() + " border should be growing");
				helper.assertValueEqual(border.getLerpTarget(), 7.0D, projection.dimension() + " border target should match");
				helper.assertValueEqual(border.getLerpTime(), WorldBorderAdapter.EXPANSION_DURATION_TICKS, projection.dimension() + " border should expand for three seconds");
			}
		} finally {
			for (WorldBorderSnapshot snapshot : previous) {
				snapshot.border().setCenter(snapshot.centerX(), snapshot.centerZ());
				snapshot.border().setSize(snapshot.size());
			}
		}

		helper.succeed();
	}

	private record WorldBorderSnapshot(WorldBorder border, double centerX, double centerZ, double size) {
	}
}
