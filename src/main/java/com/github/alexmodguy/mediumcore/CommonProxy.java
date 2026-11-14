package com.github.alexmodguy.mediumcore;

public class CommonProxy {
    private boolean mediumcoreModeLocally = false;

    public void clientInit() {
    }

    public void setGameRuleLocalValue(boolean mediumcoreMode) {
        this.mediumcoreModeLocally = mediumcoreMode;
    }

    public boolean isMediumcoreModeLocally() {
        return mediumcoreModeLocally;
    }
}