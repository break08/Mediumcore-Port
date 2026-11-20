package com.github.alexmodguy.mediumcore;

import com.github.alexmodguy.mediumcore.event.CommonEvents;
import com.github.alexmodguy.mediumcore.packet.SyncMediumcoreGameRuleMessage;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.apache.commons.lang3.tuple.Pair;

@Mod(Mediumcore.MODID)
public class Mediumcore {
    public static final String MODID = "mediumcore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static CommonProxy PROXY = new CommonProxy();

    public static final ModConfigSpec CONFIG_SPEC;
    public static final MediumcoreConfig CONFIG;

    static {
        {
            final Pair<MediumcoreConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(MediumcoreConfig::new);
            CONFIG = specPair.getLeft();
            CONFIG_SPEC = specPair.getRight();
        }
    }

    public Mediumcore(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerPayloads);

        NeoForge.EVENT_BUS.register(this);

        modEventBus.addListener((RegisterEvent event) -> {
            if (event.getRegistryKey().equals(Registries.GAME_RULE)) {
                GameRuleRegistry.setup();
            }
        });

        modContainer.registerConfig(ModConfig.Type.COMMON, CONFIG_SPEC);

        if (FMLEnvironment.getDist() == Dist.DEDICATED_SERVER) {
            PROXY.commonInit();
        }
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            PROXY.clientInit();
        }

        CommonEvents.registerAll();
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(SyncMediumcoreGameRuleMessage.TYPE,
                SyncMediumcoreGameRuleMessage.STREAM_CODEC,
                SyncMediumcoreGameRuleMessage::handle);
    }


    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }
    }

    public static <CustomPacketPayload> void sendMSGToAll(net.minecraft.network.protocol.common.custom.CustomPacketPayload message) {
        assert ServerLifecycleHooks.getCurrentServer() != null;
        sendMSGToAll(ServerLifecycleHooks.getCurrentServer(), message);
    }

    public static void sendMSGToAll(MinecraftServer server, CustomPacketPayload message) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendNonLocal(message, player);
        }
    }

    public static void sendNonLocal(CustomPacketPayload msg, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, msg);
    }

}
