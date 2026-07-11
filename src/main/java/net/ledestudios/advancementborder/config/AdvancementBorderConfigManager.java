package net.ledestudios.advancementborder.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;

import net.fabricmc.loader.api.FabricLoader;

public final class AdvancementBorderConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Set<String> KNOWN_FIELDS = Set.of(
			"schemaVersion",
			"initialDiameter",
			"growthPerAdvancement",
			"expansionDurationSeconds",
			"endCenterBlock"
	);

	private final Logger logger;
	private final Path path;

	public AdvancementBorderConfigManager(Logger logger) {
		this(logger, FabricLoader.getInstance().getConfigDir().resolve("advancementborder.json"));
	}

	AdvancementBorderConfigManager(Logger logger, Path path) {
		this.logger = logger;
		this.path = path;
	}

	public Path path() {
		return path;
	}

	public ConfigLoadResult load() {
		try {
			if (Files.notExists(path)) {
				writeDefault();
				return ConfigLoadResult.success(AdvancementBorderConfig.DEFAULT, "기본 설정 파일을 생성했습니다: " + path);
			}

			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				JsonElement parsed = JsonParser.parseReader(reader);
				if (!parsed.isJsonObject()) {
					return ConfigLoadResult.failure("설정 파일의 최상위 값은 JSON 객체여야 합니다: " + path);
				}

				JsonObject object = parsed.getAsJsonObject();
				for (String key : object.keySet()) {
					if (!KNOWN_FIELDS.contains(key)) {
						logger.warn("Unknown AdvancementBorder config field '{}' will be ignored", key);
					}
				}

				int schemaVersion = integerOrDefault(object, "schemaVersion", AdvancementBorderConfig.CURRENT_SCHEMA_VERSION);
				int initialDiameter = integerOrDefault(object, "initialDiameter", AdvancementBorderConfig.DEFAULT.initialDiameter());
				int growth = integerOrDefault(object, "growthPerAdvancement", AdvancementBorderConfig.DEFAULT.growthPerAdvancement());
				int expansionDurationSeconds = integerOrDefault(
						object,
						"expansionDurationSeconds",
						AdvancementBorderConfig.DEFAULT.expansionDurationSeconds()
				);
				AdvancementBorderConfig.EndCenterBlock end = readEndCenter(object);

				AdvancementBorderConfig config = new AdvancementBorderConfig(
						schemaVersion,
						initialDiameter,
						growth,
						expansionDurationSeconds,
						end
				);
				return ConfigLoadResult.success(config, "설정 파일을 불러왔습니다: " + path);
			}
		} catch (Exception exception) {
			return ConfigLoadResult.failure("설정 파일을 불러오지 못했습니다: " + path + " (" + exception.getMessage() + ")");
		}
	}

	public ConfigSaveResult save(AdvancementBorderConfig config) {
		Path temporaryPath = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(
					temporaryPath,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING,
					StandardOpenOption.WRITE
			)) {
				GSON.toJson(config, writer);
			}

			try {
				Files.move(temporaryPath, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
				Files.move(temporaryPath, path, StandardCopyOption.REPLACE_EXISTING);
			}
			return ConfigSaveResult.success("설정 파일을 저장했습니다: " + path);
		} catch (Exception exception) {
			try {
				Files.deleteIfExists(temporaryPath);
			} catch (IOException cleanupException) {
				logger.warn("Failed to clean up temporary AdvancementBorder config file", cleanupException);
			}
			return ConfigSaveResult.failure("설정 파일을 저장하지 못했습니다: " + path + " (" + exception.getMessage() + ")");
		}
	}

	private AdvancementBorderConfig.EndCenterBlock readEndCenter(JsonObject root) {
		JsonElement element = root.get("endCenterBlock");
		if (element == null || element.isJsonNull()) {
			return AdvancementBorderConfig.DEFAULT.endCenterBlock();
		}
		if (!element.isJsonObject()) {
			throw new IllegalArgumentException("endCenterBlock은 JSON 객체여야 합니다.");
		}

		JsonObject object = element.getAsJsonObject();
		for (String key : object.keySet()) {
			if (!key.equals("x") && !key.equals("z")) {
				logger.warn("Unknown AdvancementBorder endCenterBlock field '{}' will be ignored", key);
			}
		}
		int x = integerOrDefault(object, "x", AdvancementBorderConfig.DEFAULT.endCenterBlock().x());
		int z = integerOrDefault(object, "z", AdvancementBorderConfig.DEFAULT.endCenterBlock().z());
		return new AdvancementBorderConfig.EndCenterBlock(x, z);
	}

	private static int integerOrDefault(JsonObject object, String key, int defaultValue) {
		JsonElement element = object.get(key);
		if (element == null || element.isJsonNull()) {
			return defaultValue;
		}
		if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
			throw new IllegalArgumentException(key + " 값은 정수여야 합니다.");
		}

		try {
			return element.getAsBigDecimal().intValueExact();
		} catch (ArithmeticException | NumberFormatException exception) {
			throw new IllegalArgumentException(key + " 값은 정수여야 합니다.", exception);
		}
	}

	private void writeDefault() throws IOException {
		Files.createDirectories(path.getParent());
		try (Writer writer = Files.newBufferedWriter(
				path,
				StandardCharsets.UTF_8,
				StandardOpenOption.CREATE_NEW,
				StandardOpenOption.WRITE
		)) {
			GSON.toJson(AdvancementBorderConfig.DEFAULT, writer);
		}
	}
}
