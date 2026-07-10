package net.ledestudios.advancementborder.config;

public record ConfigLoadResult(boolean success, AdvancementBorderConfig config, String message) {
	public static ConfigLoadResult success(AdvancementBorderConfig config, String message) {
		return new ConfigLoadResult(true, config, message);
	}

	public static ConfigLoadResult failure(String message) {
		return new ConfigLoadResult(false, null, message);
	}
}
