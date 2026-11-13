package com.github.alexmodguy.mediumcore;

import com.github.alexmodguy.mediumcore.event.CommonEvents;
import net.neoforged.neoforge.common.NeoForge;

public class CommonProxy {
    public void commonInit() {
        NeoForge.EVENT_BUS.register(new CommonEvents());
    }

    public void clientInit() {
    }

    public void setGameRuleLocalValue(boolean mediumcoreMode) {
    }

    public boolean isMediumcoreModeLocally() {
        return false;
    }
}