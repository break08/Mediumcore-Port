package com.github.alexmodguy.mediumcore;

public class CommonProxy {
    private static boolean mediumcoreModeLocally = false;

    public void clientInit() {
    }

    public void setGameRuleLocalValue(boolean mediumcoreMode) {
        mediumcoreModeLocally = mediumcoreMode;
    }

    public static boolean isMediumcoreModeLocally() {
        return mediumcoreModeLocally;
    }
}