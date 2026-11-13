package com.github.alexmodguy.mediumcore;

import com.github.alexmodguy.mediumcore.packet.SyncMediumcoreGameRuleMessage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class MediumcoreClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(SyncMediumcoreGameRuleMessage.TYPE, SyncMediumcoreGameRuleMessage.STREAM_CODEC);
    }
}
