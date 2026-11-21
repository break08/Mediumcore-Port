/*
File added on 13/11/2026
Credit to Txni for Fabric Gui Mixin code: https://github.com/txnimc/Mediumcore/blob/master/src/main/java/com/github/alexmodguy/mediumcore/mixins/GuiMixin.java
 */
package com.github.alexmodguy.mediumcore.mixins.client;
import com.github.alexmodguy.mediumcore.Mediumcore;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Hud.HeartType.class)
public class GuiMixin
{
    @Unique private final Identifier mediumcore$Full = Identifier.fromNamespaceAndPath("mediumcore", "heart/full");
    @Unique private final Identifier mediumcore$Half = Identifier.fromNamespaceAndPath("mediumcore", "heart/half");
    @Unique private final Identifier mediumcore$FullBlinking = Identifier.fromNamespaceAndPath("mediumcore", "heart/hardcore_full");
    @Unique private final Identifier mediumcore$HalfBlinking = Identifier.fromNamespaceAndPath("mediumcore", "heart/hardcore_half");

    @Inject(at = @At("HEAD"), method = "getSprite", cancellable = true)
    public void getSpriteMediumcore(boolean hardcore, boolean halfHeart, boolean blinking, CallbackInfoReturnable<Identifier> cir)
    {
        if ((Object) this != Hud.HeartType.NORMAL) {
            return;
        }

        if (!Mediumcore.PROXY.isMediumcoreModeLocally()) return;

        if(!Mediumcore.CONFIG.mediumcoreHeartTexture.get()){
            return;
        }

        if (halfHeart) {
            cir.setReturnValue(blinking ? this.mediumcore$HalfBlinking : mediumcore$Half);
        } else {
            cir.setReturnValue(blinking ? this.mediumcore$FullBlinking : mediumcore$Full);
        }
    }
}