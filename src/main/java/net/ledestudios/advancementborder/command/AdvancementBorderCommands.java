package net.ledestudios.advancementborder.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.ledestudios.advancementborder.AdvancementBorderMod;
import net.ledestudios.advancementborder.runtime.AdvancementBorderRuntime;
import net.ledestudios.advancementborder.runtime.OperationResult;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;

public final class AdvancementBorderCommands {
	private AdvancementBorderCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("advboard")
				.then(Commands.literal("status")
						.executes(context -> status(context.getSource())))
				.then(Commands.literal("set")
						.then(Commands.argument("pos", BlockPosArgument.blockPos())
								.executes(context -> configure(
										context.getSource(),
										BlockPosArgument.getBlockPos(context, "pos")
								))))
				.then(Commands.literal("recalc")
						.executes(context -> respond(context.getSource(), runtime(context.getSource()).recalculate())))
				.then(Commands.literal("reload")
						.executes(context -> respond(context.getSource(), runtime(context.getSource()).reload())))
				.then(Commands.literal("config")
						.executes(context -> configStatus(context.getSource()))
						.then(Commands.literal("show")
								.executes(context -> configStatus(context.getSource())))
						.then(Commands.literal("initialDiameter")
								.then(Commands.argument("blocks", IntegerArgumentType.integer(1))
										.executes(context -> respond(
												context.getSource(),
												runtime(context.getSource()).updateInitialDiameter(IntegerArgumentType.getInteger(context, "blocks"))
										))))
						.then(Commands.literal("growthPerAdvancement")
								.then(Commands.argument("blocks", IntegerArgumentType.integer(1))
										.executes(context -> respond(
												context.getSource(),
												runtime(context.getSource()).updateGrowthPerAdvancement(IntegerArgumentType.getInteger(context, "blocks"))
										))))
						.then(Commands.literal("expansionDurationSeconds")
								.then(Commands.argument("seconds", IntegerArgumentType.integer(1))
										.executes(context -> respond(
												context.getSource(),
												runtime(context.getSource()).updateExpansionDurationSeconds(IntegerArgumentType.getInteger(context, "seconds"))
										))))
						.then(Commands.literal("endCenterBlock")
								.then(Commands.argument("x", IntegerArgumentType.integer())
										.then(Commands.argument("z", IntegerArgumentType.integer())
												.executes(context -> respond(
														context.getSource(),
														runtime(context.getSource()).updateEndCenterBlock(
																IntegerArgumentType.getInteger(context, "x"),
																IntegerArgumentType.getInteger(context, "z")
														)
								))))))
		);
	}

	private static int configure(CommandSourceStack source, net.minecraft.core.BlockPos pos) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		return respond(source, runtime(source).configure(source.getPlayerOrException(), pos));
	}

	private static int status(CommandSourceStack source) {
		source.sendSuccess(() -> Component.literal(runtime(source).status()), false);
		return 1;
	}

	private static int configStatus(CommandSourceStack source) {
		source.sendSuccess(() -> Component.literal(runtime(source).configStatus()), false);
		return 1;
	}

	private static int respond(CommandSourceStack source, OperationResult result) {
		if (result.success()) {
			source.sendSuccess(() -> Component.literal(result.message()), false);
			return 1;
		}
		source.sendFailure(Component.literal(result.message()));
		return 0;
	}

	private static AdvancementBorderRuntime runtime(CommandSourceStack source) {
		return AdvancementBorderMod.runtime(source.getServer());
	}
}
