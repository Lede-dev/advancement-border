package net.ledestudios.advancementborder.adapter;

import java.util.List;

import net.ledestudios.advancementborder.data.DimensionKind;
import net.ledestudios.advancementborder.data.DimensionProjectionData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;

public final class WorldBorderAdapter {
	private WorldBorderAdapter() {
	}

	public static void apply(MinecraftServer server, List<DimensionProjectionData> projections) {
		for (DimensionProjectionData projection : projections) {
			ServerLevel level = level(server, projection.dimension());
			if (level == null) {
				continue;
			}

			WorldBorder border = level.getWorldBorder();
			border.setCenter(projection.centerX(), projection.centerZ());
			border.setSize(projection.diameter());
		}
	}

	public static ServerLevel level(MinecraftServer server, DimensionKind dimension) {
		return switch (dimension) {
			case OVERWORLD -> server.getLevel(Level.OVERWORLD);
			case NETHER -> server.getLevel(Level.NETHER);
			case END -> server.getLevel(Level.END);
		};
	}

	public static DimensionKind kind(ServerLevel level) {
		if (level.dimension().equals(Level.OVERWORLD)) {
			return DimensionKind.OVERWORLD;
		}
		if (level.dimension().equals(Level.NETHER)) {
			return DimensionKind.NETHER;
		}
		if (level.dimension().equals(Level.END)) {
			return DimensionKind.END;
		}
		return null;
	}
}
