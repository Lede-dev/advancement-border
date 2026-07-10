package net.ledestudios.advancementborder.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

class AdvancementBorderConfigManagerTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void createsAndLoadsDefaultConfig() {
		Path path = temporaryDirectory.resolve("advancementborder.json");
		AdvancementBorderConfigManager manager = new AdvancementBorderConfigManager(LoggerFactory.getLogger("config-test"), path);

		ConfigLoadResult result = manager.load();

		assertTrue(result.success());
		assertTrue(Files.exists(path));
		assertEquals(1, result.config().initialDiameter());
		assertEquals(2, result.config().growthPerAdvancement());
	}

	@Test
	void fillsMissingFieldsFromDefaults() throws IOException {
		Path path = temporaryDirectory.resolve("partial.json");
		Files.writeString(path, "{\"growthPerAdvancement\":1}", StandardCharsets.UTF_8);
		AdvancementBorderConfigManager manager = new AdvancementBorderConfigManager(LoggerFactory.getLogger("config-test"), path);

		ConfigLoadResult result = manager.load();

		assertTrue(result.success());
		assertEquals(1, result.config().initialDiameter());
		assertEquals(1, result.config().growthPerAdvancement());
		assertEquals(100, result.config().endCenterBlock().x());
	}

	@Test
	void rejectsFractionalAndOutOfRangeValues() throws IOException {
		Path fractionalPath = temporaryDirectory.resolve("fractional.json");
		Files.writeString(fractionalPath, "{\"growthPerAdvancement\":1.5}", StandardCharsets.UTF_8);
		AdvancementBorderConfigManager fractional = new AdvancementBorderConfigManager(LoggerFactory.getLogger("config-test"), fractionalPath);
		assertFalse(fractional.load().success());

		Path rangePath = temporaryDirectory.resolve("range.json");
		Files.writeString(rangePath, "{\"growthPerAdvancement\":0}", StandardCharsets.UTF_8);
		AdvancementBorderConfigManager range = new AdvancementBorderConfigManager(LoggerFactory.getLogger("config-test"), rangePath);
		assertFalse(range.load().success());
	}

	@Test
	void savesConfigAtomicallyAndLoadsUpdatedValues() {
		Path path = temporaryDirectory.resolve("saved.json");
		AdvancementBorderConfigManager manager = new AdvancementBorderConfigManager(LoggerFactory.getLogger("config-test"), path);
		AdvancementBorderConfig updated = new AdvancementBorderConfig(
				1,
				5,
				1,
				new AdvancementBorderConfig.EndCenterBlock(80, -12)
		);

		ConfigSaveResult saved = manager.save(updated);
		ConfigLoadResult loaded = manager.load();

		assertTrue(saved.success());
		assertTrue(loaded.success());
		assertEquals(5, loaded.config().initialDiameter());
		assertEquals(1, loaded.config().growthPerAdvancement());
		assertEquals(80, loaded.config().endCenterBlock().x());
		assertEquals(-12, loaded.config().endCenterBlock().z());
		assertFalse(Files.exists(path.resolveSibling("saved.json.tmp")));
	}
}
