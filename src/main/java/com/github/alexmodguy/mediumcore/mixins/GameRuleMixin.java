package com.github.alexmodguy.mediumcore.mixins;

import com.github.alexmodguy.mediumcore.GameRuleRegistry;
import com.github.alexmodguy.mediumcore.Mediumcore;
import com.github.alexmodguy.mediumcore.packet.SyncMediumcoreGameRuleMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRules.class)
public class GameRuleMixin {
    @Inject(method = "set", at = @At("HEAD"))
    private <T> void set(GameRule<T> gameRule, T object, @Nullable MinecraftServer server, CallbackInfo ci){
        if (gameRule == GameRuleRegistry.getMediumcoreGamerule() && server != null){
                Mediumcore.sendMSGToAll(server, new SyncMediumcoreGameRuleMessage((Boolean) object));
        }
    }
}