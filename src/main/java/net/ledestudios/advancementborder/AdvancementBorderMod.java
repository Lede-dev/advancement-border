package net.ledestudios.advancementborder;

import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ledestudios.advancementborder.command.AdvancementBorderCommands;
import net.ledestudios.advancementborder.runtime.AdvancementBorderRuntime;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class AdvancementBorderMod implements ModInitializer {
	public static final String MOD_ID = "advancementborder";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final Map<MinecraftServer, AdvancementBorderRuntime> RUNTIMES = new WeakHashMap<>();

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> AdvancementBorderCommands.register(dispatcher));

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			AdvancementBorderRuntime runtime = new AdvancementBorderRuntime(server, LOGGER);
			RUNTIMES.put(server, runtime);
			runtime.start();
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(RUNTIMES::remove);
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (success) {
				runtime(server).rebuildCatalog();
			}
		});
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
			if (joined) {
				runtime(player.level().getServer()).onPlayerJoin(player);
			}
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			AdvancementBorderRuntime runtime = RUNTIMES.get(server);
			if (runtime != null) {
				runtime.tick();
			}
		});
	}

	public static AdvancementBorderRuntime runtime(MinecraftServer server) {
		AdvancementBorderRuntime runtime = RUNTIMES.get(server);
		if (runtime == null) {
			throw new IllegalStateException("AdvancementBorder runtime is not available");
		}
		return runtime;
	}

	public static void markAdvancementDirty(ServerPlayer player) {
		AdvancementBorderRuntime runtime = RUNTIMES.get(player.level().getServer());
		if (runtime != null) {
			runtime.markDirty(player);
		}
	}
}
