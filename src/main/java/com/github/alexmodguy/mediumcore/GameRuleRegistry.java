package com.github.alexmodguy.mediumcore;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public class GameRuleRegistry {
    private static GameRule<Boolean> mediumcoreMode;

    public static void setup() {
        mediumcoreMode = createBoolean("mediumcoreMode", false, GameRuleCategory.PLAYER);
    }

    public static GameRule<Boolean> createBoolean(String id, boolean defaultVal, GameRuleCategory cat) {
        /*
        GameRule<Boolean> ruleTypeBoolean = GameRules.BooleanValue.create(defaultVal, ((server, booleanValue) -> {
            Mediumcore.sendMSGToAll(server, new SyncMediumcoreGameRuleMessage(booleanValue.get()));
        }));

         */
        GameRule<Boolean> ruleTypeBoolean = GameRuleBuilder
                .forBoolean(defaultVal)
                .category(cat)
                .buildAndRegister(Identifier.fromNamespaceAndPath(Mediumcore.MODID, id));
        Mediumcore.LOGGER.info("Created game rule {}", id);
        return ruleTypeBoolean;
    }

    public static GameRule<Boolean> getMediumcoreGamerule(){
        return mediumcoreMode;
    }

    public static boolean isMediumCoreMode(GameRules gameRules){
        return mediumcoreMode != null && gameRules.get(mediumcoreMode);
    }

    public static void setMediumcoreMode(GameRules gameRules, boolean mediumcore, @Nullable MinecraftServer minecraftServer){
        if(mediumcoreMode != null){
            gameRules.set(mediumcoreMode, mediumcore, minecraftServer);
        }
    }

    /*
    @Override
    public void onInitialize() {
        mediumcoreMode = createBoolean("mediumcore_mode", false, GameRuleCategory.PLAYER);
    }

     */
}