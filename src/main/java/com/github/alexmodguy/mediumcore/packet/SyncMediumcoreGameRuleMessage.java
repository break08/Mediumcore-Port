package com.github.alexmodguy.mediumcore.packet;

import com.github.alexmodguy.mediumcore.Mediumcore;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class SyncMediumcoreGameRuleMessage implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncMediumcoreGameRuleMessage> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Mediumcore.MODID, "sync_mediumcore_game_rule"));

    public static final StreamCodec<FriendlyByteBuf, SyncMediumcoreGameRuleMessage> STREAM_CODEC =
            StreamCodec.ofMember(SyncMediumcoreGameRuleMessage::write, SyncMediumcoreGameRuleMessage::read);

    private boolean mediumcoreMode;

    public SyncMediumcoreGameRuleMessage(boolean mediumcoreMode) {
        this.mediumcoreMode = mediumcoreMode;
    }

    public static SyncMediumcoreGameRuleMessage read(FriendlyByteBuf buf) {
        return new SyncMediumcoreGameRuleMessage(buf.readBoolean());
    }

    public static void write(SyncMediumcoreGameRuleMessage message, FriendlyByteBuf buf) {
        buf.writeBoolean(message.mediumcoreMode);
    }

    public static void handle(SyncMediumcoreGameRuleMessage message, ClientPlayNetworking.Context context) {
        context.client().execute(() -> Mediumcore.PROXY.setGameRuleLocalValue(message.mediumcoreMode));
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}