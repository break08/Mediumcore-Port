package com.github.alexmodguy.mediumcore.mixins;

import com.github.alexmodguy.mediumcore.FabricIsSoBadAtDataAttach;
import com.github.alexmodguy.mediumcore.GameRuleRegistry;
import com.github.alexmodguy.mediumcore.Mediumcore;
import com.github.alexmodguy.mediumcore.MediumcoreTags;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.alexmodguy.mediumcore.FabricIsSoBadAtDataAttach.PERSISTED_NBT_TAG;
import static com.github.alexmodguy.mediumcore.event.CommonEvents.HEALTH_MODIFIER_TAG;
import static com.github.alexmodguy.mediumcore.event.CommonEvents.updateHealth;

@Mixin(LivingEntity.class)
public class FinishedYourMeal {
    @Inject(method = "completeUsingItem", at = @At("HEAD"))
    public void restoreMaxHealth(CallbackInfo ci){
        LivingEntity living = (LivingEntity)(Object)this;
        if (living instanceof Player player && !player.level().isClientSide) {
            if (player.getUseItem().is(MediumcoreTags.RESTORES_MAX_HEALTH) && GameRuleRegistry.isMediumCoreMode(player.level().getGameRules())) {
                CompoundTag tag = FabricIsSoBadAtDataAttach.getPersisted(player);
                double clampedHealth = Mth.clamp(player.getMaxHealth() + Mediumcore.CONFIG.healthIncreasePerHeal.get(), Mediumcore.CONFIG.minimumPlayerHealth.get(), Mediumcore.CONFIG.maxPlayerHealth.get());
                double healthModifiedBy = tag.getDouble(HEALTH_MODIFIER_TAG) + (clampedHealth - player.getMaxHealth());
                tag.putDouble(HEALTH_MODIFIER_TAG, healthModifiedBy);
                FabricIsSoBadAtDataAttach.getPersistentData(player).put(PERSISTED_NBT_TAG, tag);
                updateHealth(player, healthModifiedBy);
            }
        }
    }
}
