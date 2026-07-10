package net.ledestudios.advancementborder.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import net.ledestudios.advancementborder.adapter.MinecraftAdvancementAdapter;
import net.ledestudios.advancementborder.adapter.SafeRelocationAdapter;
import net.ledestudios.advancementborder.adapter.WorldBorderAdapter;
import net.ledestudios.advancementborder.config.AdvancementBorderConfig;
import net.ledestudios.advancementborder.config.AdvancementBorderConfigManager;
import net.ledestudios.advancementborder.config.ConfigLoadResult;
import net.ledestudios.advancementborder.config.ConfigSaveResult;
import net.ledestudios.advancementborder.data.AdvancementCatalogData;
import net.ledestudios.advancementborder.data.BorderRuleData;
import net.ledestudios.advancementborder.data.BorderStateData;
import net.ledestudios.advancementborder.data.DimensionKind;
import net.ledestudios.advancementborder.data.DimensionProjectionData;
import net.ledestudios.advancementborder.data.PlayerProgressData;
import net.ledestudios.advancementborder.persistence.AdvancementBorderSavedData;
import net.ledestudios.advancementborder.system.AdvancementCountSystem;
import net.ledestudios.advancementborder.system.BorderMath;
import net.ledestudios.advancementborder.system.DimensionProjectionSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class AdvancementBorderRuntime {
	private final MinecraftServer server;
	private final Logger logger;
	private final AdvancementBorderConfigManager configManager;
	private AdvancementBorderConfig config = AdvancementBorderConfig.DEFAULT;
	private BorderRuleData rules = BorderRuleData.DEFAULT;
	private AdvancementCatalogData catalog;
	private AdvancementBorderSavedData savedData;
	private boolean dirty;

	public AdvancementBorderRuntime(MinecraftServer server, Logger logger) {
		this.server = server;
		this.logger = logger;
		this.configManager = new AdvancementBorderConfigManager(logger);
	}

	public void start() {
		ConfigLoadResult loaded = configManager.load();
		if (loaded.success()) {
			config = loaded.config();
			rules = config.toRules();
			logger.info(loaded.message());
		} else {
			logger.error("{}; using built-in defaults", loaded.message());
		}

		savedData = AdvancementBorderSavedData.get(server);
		rebuildCatalog();
		restorePersistedBorder();
	}

	public void rebuildCatalog() {
		catalog = MinecraftAdvancementAdapter.buildCatalog(server);
		logger.info("Built AdvancementBorder catalog with {} advancements ({} eligible)", catalog.ids().size(), catalog.eligibleMask().cardinality());
		dirty = true;
	}

	public void markDirty(ServerPlayer player) {
		BorderStateData state = state();
		if (state.active() && state.ownerUuid() != null && state.ownerUuid().equals(player.getUUID())) {
			dirty = true;
		}
	}

	public void onPlayerJoin(ServerPlayer player) {
		markDirty(player);
	}

	public void tick() {
		if (!dirty) {
			return;
		}
		dirty = false;
		OperationResult result = recalculate();
		if (!result.success() && state().active()) {
			logger.debug("Deferred AdvancementBorder recalculation: {}", result.message());
		}
	}

	public OperationResult configure(ServerPlayer player, BlockPos anchor) {
		if (!player.level().dimension().equals(Level.OVERWORLD)) {
			return OperationResult.failure("이 명령어는 오버월드에서만 사용할 수 있습니다.");
		}

		savedData.configure(player.getUUID(), anchor.getX(), anchor.getY(), anchor.getZ());
		return recalculate();
	}

	public OperationResult recalculate() {
		BorderStateData current = state();
		if (!current.active()) {
			return OperationResult.failure("AdvancementBorder가 설정되지 않았습니다. 먼저 /advboard set을 사용하세요.");
		}
		if (current.ownerUuid() == null) {
			return fail("저장된 소유자 UUID가 올바르지 않습니다.");
		}

		ServerPlayer owner = server.getPlayerList().getPlayer(current.ownerUuid());
		if (owner == null) {
			return OperationResult.failure("설정된 소유자가 접속해 있지 않습니다.");
		}

		PlayerProgressData progress = MinecraftAdvancementAdapter.snapshotProgress(owner, catalog);
		int completedCount = AdvancementCountSystem.count(catalog, progress);
		double diameter = BorderMath.calculateDiameter(rules, completedCount);
		List<DimensionProjectionData> projections = DimensionProjectionSystem.project(
				current.overworldCenterX(),
				current.overworldCenterZ(),
				diameter,
				rules
		);

		OperationResult relocation = relocateIfNeeded(owner, current, projections);
		if (!relocation.success()) {
			return relocation;
		}

		WorldBorderAdapter.apply(server, projections);
		savedData.recordApplied(completedCount, diameter);

		return OperationResult.success("완료 발전과제 " + completedCount + "개, 보더 한 변 " + formatDiameter(diameter) + "블록으로 동기화했습니다.");
	}

	public OperationResult reload() {
		ConfigLoadResult loaded = configManager.load();
		if (!loaded.success()) {
			return OperationResult.failure(loaded.message());
		}

		config = loaded.config();
		rules = config.toRules();
		OperationResult recalculated = recalculate();
		if (!recalculated.success()) {
			return OperationResult.failure("설정은 불러왔지만 보더 적용에 실패했습니다: " + recalculated.message());
		}
		return OperationResult.success("설정을 다시 불러왔습니다. " + recalculated.message());
	}

	public OperationResult updateInitialDiameter(int initialDiameter) {
		return applyUpdatedConfig(
				new AdvancementBorderConfig(
						config.schemaVersion(),
						initialDiameter,
						config.growthPerAdvancement(),
						config.endCenterBlock()
				),
				"초기 보더 크기를 " + initialDiameter + "블록으로 변경했습니다."
		);
	}

	public OperationResult updateGrowthPerAdvancement(int growthPerAdvancement) {
		return applyUpdatedConfig(
				new AdvancementBorderConfig(
						config.schemaVersion(),
						config.initialDiameter(),
						growthPerAdvancement,
						config.endCenterBlock()
				),
				"발전과제당 증가량을 " + growthPerAdvancement + "블록으로 변경했습니다."
		);
	}

	public OperationResult updateEndCenterBlock(int x, int z) {
		return applyUpdatedConfig(
				new AdvancementBorderConfig(
						config.schemaVersion(),
						config.initialDiameter(),
						config.growthPerAdvancement(),
						new AdvancementBorderConfig.EndCenterBlock(x, z)
				),
				"엔드 중심 블록을 " + x + ", " + z + "로 변경했습니다."
		);
	}

	public String configStatus() {
		return String.join("\n", List.of(
				"[AdvancementBorder 설정]",
				"초기 보더 크기: " + config.initialDiameter() + "블록",
				"발전과제당 증가량: " + config.growthPerAdvancement() + "블록",
				"엔드 중심 블록: " + config.endCenterBlock().x() + ", " + config.endCenterBlock().z(),
				"설정 파일: " + configManager.path()
		));
	}

	public String status() {
		BorderStateData state = state();
		List<String> lines = new ArrayList<>();
		lines.add("[AdvancementBorder 상태]");

		if (!state.active()) {
			lines.add("활성 여부: 비활성");
			lines.add("설정 방법: /advboard set <x> <y> <z>");
			lines.add("초기 보더 크기: " + rules.initialDiameter() + "블록");
			lines.add("발전과제당 증가량: " + rules.growthPerAdvancement() + "블록");
			return String.join("\n", lines);
		}

		lines.add("활성 여부: 활성");
		lines.add("소유자 UUID: " + state.ownerUuid());
		lines.add("완료 발전과제: " + state.completedCount() + "개");
		lines.add("초기 보더 크기: " + rules.initialDiameter() + "블록");
		lines.add("발전과제당 증가량: " + rules.growthPerAdvancement() + "블록");
		lines.add("목표 보더 크기: " + BorderMath.calculateDiameter(rules, state.completedCount()) + "블록");
		lines.add("적용 보더 크기: " + formatDiameter(state.appliedDiameter()) + "블록");
		lines.add("오버월드 중심: " + state.overworldCenterX() + ", " + state.overworldCenterZ());
		lines.add("네더 중심: " + state.overworldCenterX() + ", " + state.overworldCenterZ());
		lines.add("엔드 중심: " + (rules.endCenterBlockX() + 0.5D) + ", " + (rules.endCenterBlockZ() + 0.5D));

		if (!state.lastError().isBlank()) {
			lines.add("마지막 오류: " + state.lastError());
		}

		for (DimensionKind kind : DimensionKind.values()) {
			var level = WorldBorderAdapter.level(server, kind);
			if (level != null) {
				lines.add(dimensionLabel(kind) + " 실제 보더 크기: " + formatDiameter(level.getWorldBorder().getSize()) + "블록");
			}
		}
		return String.join("\n", lines);
	}

	public BorderStateData state() {
		if (savedData == null) {
			throw new IllegalStateException("AdvancementBorder runtime has not started");
		}
		return savedData.snapshot();
	}

	private void restorePersistedBorder() {
		BorderStateData state = state();
		if (!state.active()) {
			return;
		}

		double diameter = state.appliedDiameter() > 0.0D
				? state.appliedDiameter()
				: BorderMath.calculateDiameter(rules, state.completedCount());
		WorldBorderAdapter.apply(server, DimensionProjectionSystem.project(
				state.overworldCenterX(),
				state.overworldCenterZ(),
				diameter,
				rules
		));
	}

	private OperationResult relocateIfNeeded(
			ServerPlayer owner,
			BorderStateData state,
			List<DimensionProjectionData> projections
	) {
		DimensionKind ownerDimension = WorldBorderAdapter.kind(owner.level());
		if (ownerDimension == null) {
			return OperationResult.success("소유자가 관리 대상 외 차원에 있습니다.");
		}

		Optional<DimensionProjectionData> projection = projections.stream()
				.filter(candidate -> candidate.dimension() == ownerDimension)
				.findFirst();
		if (projection.isEmpty() || SafeRelocationAdapter.isInside(owner, projection.get())) {
			return OperationResult.success("안전 이동이 필요하지 않습니다.");
		}

		Optional<Vec3> safePosition = SafeRelocationAdapter.findSafePosition(owner, projection.get());
		if (safePosition.isEmpty()) {
			return fail("안전한 이동 위치를 찾지 못해 보더 변경을 취소했습니다.");
		}

		Vec3 target = safePosition.get();
		owner.teleportTo(target.x(), target.y(), target.z());
		return OperationResult.success("소유자를 새 보더 안으로 이동했습니다.");
	}

	private OperationResult fail(String message) {
		savedData.recordError(message);
		logger.error(message);
		return OperationResult.failure(message);
	}

	private OperationResult applyUpdatedConfig(AdvancementBorderConfig updatedConfig, String changeMessage) {
		ConfigSaveResult saved = configManager.save(updatedConfig);
		if (!saved.success()) {
			return OperationResult.failure(saved.message());
		}

		config = updatedConfig;
		rules = updatedConfig.toRules();
		BorderStateData state = state();
		if (!state.active()) {
			return OperationResult.success(changeMessage + " 월드보더 설정 후 적용됩니다.");
		}

		if (state.ownerUuid() == null || server.getPlayerList().getPlayer(state.ownerUuid()) == null) {
			dirty = true;
			return OperationResult.success(changeMessage + " 소유자가 접속하면 월드보더를 다시 계산합니다.");
		}

		OperationResult recalculated = recalculate();
		if (!recalculated.success()) {
			return OperationResult.failure(changeMessage + " 설정은 저장했지만 현재 월드보더에 적용하지 못했습니다: " + recalculated.message());
		}
		return OperationResult.success(changeMessage + " " + recalculated.message());
	}

	private static String formatDiameter(double diameter) {
		long integral = (long) diameter;
		return diameter == integral ? Long.toString(integral) : Double.toString(diameter);
	}

	private static String dimensionLabel(DimensionKind kind) {
		return switch (kind) {
			case OVERWORLD -> "오버월드";
			case NETHER -> "네더";
			case END -> "엔드";
		};
	}
}
