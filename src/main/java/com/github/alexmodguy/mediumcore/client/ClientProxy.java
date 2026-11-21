package com.github.alexmodguy.mediumcore.client;

import com.github.alexmodguy.mediumcore.CommonProxy;

public class ClientProxy extends CommonProxy {
    private boolean mediumcoreMode;

    public void clientInit() {
    }

    public void setGameRuleLocalValue(boolean mediumcoreMode) {
        this.mediumcoreMode = mediumcoreMode;
    }

    public boolean isMediumcoreModeLocally() {
        return this.mediumcoreMode;
    }

}