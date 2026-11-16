package com.github.alexmodguy.mediumcore.mixins;

import com.github.alexmodguy.mediumcore.FabricIsSoBadAtDataAttach;
import com.github.alexmodguy.mediumcore.GameRuleRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.alexmodguy.mediumcore.event.CommonEvents.HEALTH_MODIFIER_TAG;
import static com.github.alexmodguy.mediumcore.event.CommonEvents.updateHealth;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void mediumcore$applyHealthOnRespawn(ServerPlayer that, boolean keepEverything, CallbackInfo ci) {
        if (!keepEverything && !that.level().isClientSide()) {
            if (GameRuleRegistry.isMediumCoreMode(that.level().getGameRules())) {
                CompoundTag tag = FabricIsSoBadAtDataAttach.getPersisted(that);
                double healthModifiedBy = tag.getDouble(HEALTH_MODIFIER_TAG).orElse(0.0D);
                updateHealth(that, healthModifiedBy);
            }
        }
    }
}
