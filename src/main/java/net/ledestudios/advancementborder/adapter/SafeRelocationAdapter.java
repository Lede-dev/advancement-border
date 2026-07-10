package net.ledestudios.advancementborder.adapter;

import java.util.Optional;

import net.ledestudios.advancementborder.data.DimensionProjectionData;
import net.ledestudios.advancementborder.system.SafeCandidateSystem;
import net.ledestudios.advancementborder.system.SafeCandidateSystem.Column;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public final class SafeRelocationAdapter {
	private static final double PLAYER_BORDER_INSET = 0.35D;
	private static final int LOCAL_SEARCH_RADIUS = 8;
	private static final int CENTER_SEARCH_RADIUS = 4;
	private static final int SURFACE_Y_TOLERANCE = 2;
	private static final int NETHER_SAFE_CEILING_Y = 120;

	private SafeRelocationAdapter() {
	}

	public static boolean isInside(ServerPlayer player, DimensionProjectionData target) {
		double half = target.diameter() / 2.0D;
		return player.getX() >= target.centerX() - half + PLAYER_BORDER_INSET
				&& player.getX() <= target.centerX() + half - PLAYER_BORDER_INSET
				&& player.getZ() >= target.centerZ() - half + PLAYER_BORDER_INSET
				&& player.getZ() <= target.centerZ() + half - PLAYER_BORDER_INSET;
	}

	public static Optional<Vec3> findSafePosition(
			ServerPlayer player,
			DimensionProjectionData target
	) {
		ServerLevel level = player.level();
		double half = target.diameter() / 2.0D;
		double minX = target.centerX() - half + PLAYER_BORDER_INSET;
		double maxX = target.centerX() + half - PLAYER_BORDER_INSET;
		double minZ = target.centerZ() - half + PLAYER_BORDER_INSET;
		double maxZ = target.centerZ() + half - PLAYER_BORDER_INSET;

		if (minX > maxX || minZ > maxZ) {
			return Optional.empty();
		}

		int clampedX = (int) Math.floor(clamp(player.getX(), minX, maxX));
		int clampedZ = (int) Math.floor(clamp(player.getZ(), minZ, maxZ));
		Optional<Vec3> local = search(level, clampedX, clampedZ, LOCAL_SEARCH_RADIUS, minX, maxX, minZ, maxZ);
		if (local.isPresent()) {
			return local;
		}

		int centerX = (int) Math.floor(target.centerX());
		int centerZ = (int) Math.floor(target.centerZ());
		return search(level, centerX, centerZ, CENTER_SEARCH_RADIUS, minX, maxX, minZ, maxZ);
	}

	private static Optional<Vec3> search(
			ServerLevel level,
			int originX,
			int originZ,
			int radius,
			double minX,
			double maxX,
			double minZ,
			double maxZ
	) {
		for (Column column : SafeCandidateSystem.squareSpiral(originX, originZ, radius)) {
			double x = column.x() + 0.5D;
			double z = column.z() + 0.5D;
			if (x < minX || x > maxX || z < minZ || z > maxZ) {
				continue;
			}

			level.getChunk(column.x() >> 4, column.z() >> 4);
			Optional<Integer> safeY = findSurfaceSafeY(level, column.x(), column.z());
			if (safeY.isPresent()) {
				return Optional.of(new Vec3(x, safeY.get(), z));
			}
		}

		return Optional.empty();
	}

	private static Optional<Integer> findSurfaceSafeY(ServerLevel level, int x, int z) {
		if (level.dimension().equals(Level.NETHER)) {
			return findHighestNetherSafeY(level, x, z);
		}

		int minimum = level.getMinY() + 1;
		int maximum = level.getMaxY() - 2;
		int surfaceY = Math.max(minimum, Math.min(maximum, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)));

		for (int offset = 0; offset <= SURFACE_Y_TOLERANCE; offset++) {
			int candidateY = surfaceY + offset;
			if (candidateY <= maximum && isSafe(level, new BlockPos(x, candidateY, z))) {
				return Optional.of(candidateY);
			}
		}

		return Optional.empty();
	}

	private static Optional<Integer> findHighestNetherSafeY(ServerLevel level, int x, int z) {
		int minimum = level.getMinY() + 1;
		int maximum = Math.min(level.getMaxY() - 2, NETHER_SAFE_CEILING_Y);

		for (int y = maximum; y >= minimum; y--) {
			BlockPos candidate = new BlockPos(x, y, z);
			if (isSafe(level, candidate) && !level.getBlockState(candidate.below()).is(Blocks.BEDROCK)) {
				return Optional.of(y);
			}
		}

		return Optional.empty();
	}

	private static boolean isSafe(ServerLevel level, BlockPos feetPos) {
		BlockPos floorPos = feetPos.below();
		BlockPos headPos = feetPos.above();
		BlockState floor = level.getBlockState(floorPos);
		BlockState feet = level.getBlockState(feetPos);
		BlockState head = level.getBlockState(headPos);

		return floor.isFaceSturdy(level, floorPos, Direction.UP)
				&& !isHazard(floor)
				&& feet.getCollisionShape(level, feetPos).isEmpty()
				&& head.getCollisionShape(level, headPos).isEmpty()
				&& feet.getFluidState().isEmpty()
				&& head.getFluidState().isEmpty()
				&& !isHazard(feet)
				&& !isHazard(head);
	}

	private static boolean isHazard(BlockState state) {
		return state.is(BlockTags.LEAVES)
				|| state.is(Blocks.MAGMA_BLOCK)
				|| state.is(Blocks.CACTUS)
				|| state.is(Blocks.CAMPFIRE)
				|| state.is(Blocks.SOUL_CAMPFIRE)
				|| state.is(Blocks.FIRE)
				|| state.is(Blocks.SOUL_FIRE)
				|| state.is(Blocks.POWDER_SNOW);
	}

	private static double clamp(double value, double minimum, double maximum) {
		return Math.max(minimum, Math.min(maximum, value));
	}
}
