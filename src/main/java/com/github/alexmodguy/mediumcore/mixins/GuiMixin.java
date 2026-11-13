/*
Modified from Original Mediumcore mod of AlexModGuy
Credit to Txni for Fabric Gui Mixin code: https://github.com/txnimc/Mediumcore/blob/master/src/main/java/com/github/alexmodguy/mediumcore/mixins/GuiMixin.java
 */
package com.github.alexmodguy.mediumcore.mixins;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Gui.HeartType.class)
public class GuiMixin
{
    @Unique private final ResourceLocation mediumcore$Full = ResourceLocation.withDefaultNamespace("hud/heart/mediumcore_full");
    @Unique private final ResourceLocation mediumcore$Half = ResourceLocation.withDefaultNamespace("hud/heart/mediumcore_half");
    @Unique private final ResourceLocation mediumcore$FullBlinking = ResourceLocation.withDefaultNamespace("hud/heart/mediumcore_full_blinking");
    @Unique private final ResourceLocation mediumcore$HalfBlinking = ResourceLocation.withDefaultNamespace("hud/heart/mediumcore_half_blinking") ;

    @Inject(at = @At("HEAD"), method = "getSprite", cancellable = true)
    public void getSpriteMediumcore(boolean hardcore, boolean halfHeart, boolean blinking, CallbackInfoReturnable<ResourceLocation> cir)
    {
        if ((Object) this != Gui.HeartType.NORMAL)
            return;

        if (halfHeart) {
            cir.setReturnValue(blinking ? this.mediumcore$HalfBlinking : mediumcore$Half);
        } else {
            cir.setReturnValue(blinking ? this.mediumcore$FullBlinking : mediumcore$Full);
        }
    }
}