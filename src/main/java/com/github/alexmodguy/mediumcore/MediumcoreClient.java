package com.github.alexmodguy.mediumcore;

import com.github.alexmodguy.mediumcore.packet.SyncMediumcoreGameRuleMessage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class MediumcoreClient implements ClientModInitializer {
    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncMediumcoreGameRuleMessage.TYPE, SyncMediumcoreGameRuleMessage::handle);
    }
}
