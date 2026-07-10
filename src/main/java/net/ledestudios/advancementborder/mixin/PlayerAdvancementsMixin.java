package net.ledestudios.advancementborder.mixin;

import net.ledestudios.advancementborder.AdvancementBorderMod;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin {
	@Shadow
	private ServerPlayer player;

	@Inject(method = {"award", "revoke"}, at = @At("RETURN"))
	private void advancementborder$markDirty(CallbackInfoReturnable<Boolean> callback) {
		if (Boolean.TRUE.equals(callback.getReturnValue())) {
			AdvancementBorderMod.markAdvancementDirty(player);
		}
	}
}
