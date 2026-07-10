package net.ledestudios.advancementborder.config;

public record ConfigSaveResult(boolean success, String message) {
	public static ConfigSaveResult success(String message) {
		return new ConfigSaveResult(true, message);
	}

	public static ConfigSaveResult failure(String message) {
		return new ConfigSaveResult(false, message);
	}
}
