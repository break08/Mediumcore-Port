package com.github.alexmodguy.mediumcore;

import com.github.alexmodguy.mediumcore.event.CommonEvents;
import com.github.alexmodguy.mediumcore.packet.SyncMediumcoreGameRuleMessage;
import com.mojang.logging.LogUtils;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import org.apache.commons.lang3.tuple.Pair;

public class Mediumcore implements ModInitializer {
    public static final String MODID = "mediumcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static CommonProxy PROXY = new CommonProxy();;

    public static final ModConfigSpec CONFIG_SPEC;
    public static final MediumcoreConfig CONFIG;

    static {
        {
            final Pair<MediumcoreConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(MediumcoreConfig::new);
            CONFIG = specPair.getLeft();
            CONFIG_SPEC = specPair.getRight();
        }
    }


    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        GameRuleRegistry.setup();
        PayloadTypeRegistry.playS2C().register(SyncMediumcoreGameRuleMessage.TYPE, SyncMediumcoreGameRuleMessage.STREAM_CODEC);
        CommonEvents.registerAll();
        NeoForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.SERVER, CONFIG_SPEC);
        init();
    }

    private static MinecraftServer currentServer;
    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);
    }

    public static void sendMSGToAll(CustomPacketPayload message) {
        sendMSGToAll(currentServer, message);
    }

    public static void sendMSGToAll(MinecraftServer server, CustomPacketPayload message) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendNonLocal(message, player);
        }
    }

    public static void sendNonLocal(CustomPacketPayload msg, ServerPlayer player) {
        ServerPlayNetworking.send(player, msg);
    }

}
