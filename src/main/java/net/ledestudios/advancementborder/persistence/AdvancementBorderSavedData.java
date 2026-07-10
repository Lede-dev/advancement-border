package net.ledestudios.advancementborder.persistence;

import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.ledestudios.advancementborder.AdvancementBorderMod;
import net.ledestudios.advancementborder.data.BorderStateData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class AdvancementBorderSavedData extends SavedData {
	private static final int SCHEMA_VERSION = 1;
	private static final Codec<AdvancementBorderSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.optionalFieldOf("schemaVersion", SCHEMA_VERSION).forGetter(data -> data.schemaVersion),
			Codec.BOOL.optionalFieldOf("active", false).forGetter(data -> data.active),
			Codec.STRING.optionalFieldOf("ownerUuid", "").forGetter(data -> data.ownerUuid),
			Codec.INT.optionalFieldOf("anchorX", 0).forGetter(data -> data.anchorX),
			Codec.INT.optionalFieldOf("anchorY", 0).forGetter(data -> data.anchorY),
			Codec.INT.optionalFieldOf("anchorZ", 0).forGetter(data -> data.anchorZ),
			Codec.INT.optionalFieldOf("completedCount", 0).forGetter(data -> data.completedCount),
			Codec.DOUBLE.optionalFieldOf("appliedDiameter", 0.0D).forGetter(data -> data.appliedDiameter),
			Codec.STRING.optionalFieldOf("lastError", "").forGetter(data -> data.lastError)
	).apply(instance, AdvancementBorderSavedData::new));

	private static final SavedDataType<AdvancementBorderSavedData> TYPE = new SavedDataType<>(
			Identifier.fromNamespaceAndPath(AdvancementBorderMod.MOD_ID, "state"),
			AdvancementBorderSavedData::new,
			CODEC,
			null
	);

	private int schemaVersion = SCHEMA_VERSION;
	private boolean active;
	private String ownerUuid = "";
	private int anchorX;
	private int anchorY;
	private int anchorZ;
	private int completedCount;
	private double appliedDiameter;
	private String lastError = "";

	public AdvancementBorderSavedData() {
	}

	private AdvancementBorderSavedData(
			int schemaVersion,
			boolean active,
			String ownerUuid,
			int anchorX,
			int anchorY,
			int anchorZ,
			int completedCount,
			double appliedDiameter,
			String lastError
	) {
		this.schemaVersion = schemaVersion;
		this.active = active;
		this.ownerUuid = ownerUuid;
		this.anchorX = anchorX;
		this.anchorY = anchorY;
		this.anchorZ = anchorZ;
		this.completedCount = completedCount;
		this.appliedDiameter = appliedDiameter;
		this.lastError = lastError;
	}

	public static AdvancementBorderSavedData get(MinecraftServer server) {
		ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		if (overworld == null) {
			throw new IllegalStateException("Overworld is not available");
		}
		return overworld.getDataStorage().computeIfAbsent(TYPE);
	}

	public BorderStateData snapshot() {
		return new BorderStateData(
				active,
				parseOwnerUuid(),
				anchorX,
				anchorY,
				anchorZ,
				completedCount,
				appliedDiameter,
				lastError
		);
	}

	public void configure(UUID owner, int x, int y, int z) {
		this.schemaVersion = SCHEMA_VERSION;
		this.active = true;
		this.ownerUuid = owner.toString();
		this.anchorX = x;
		this.anchorY = y;
		this.anchorZ = z;
		this.lastError = "";
		setDirty();
	}

	public void recordApplied(int completedCount, double appliedDiameter) {
		this.completedCount = completedCount;
		this.appliedDiameter = appliedDiameter;
		this.lastError = "";
		setDirty();
	}

	public void recordError(String error) {
		this.lastError = error == null ? "" : error;
		setDirty();
	}

	private UUID parseOwnerUuid() {
		if (ownerUuid == null || ownerUuid.isBlank()) {
			return null;
		}
		try {
			return UUID.fromString(ownerUuid);
		} catch (IllegalArgumentException ignored) {
			return null;
		}
	}
}
