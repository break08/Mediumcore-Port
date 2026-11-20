package com.github.alexmodguy.mediumcore.event;

import com.github.alexmodguy.mediumcore.GameRuleRegistry;
import com.github.alexmodguy.mediumcore.Mediumcore;
import com.github.alexmodguy.mediumcore.misc.DedicatedServerPropertiesAccessor;
import com.github.alexmodguy.mediumcore.packet.SyncMediumcoreGameRuleMessage;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class CommonEvents {

    public static final String HEALTH_MODIFIER_TAG = "MediumcoreHealthModifier";
    private static final Identifier INITIAL_HEALTH_MODIFIER_UUID =
            Identifier.fromNamespaceAndPath(Mediumcore.MODID, "initial_health_modifier");
    private static final Identifier HEALTH_MODIFIER_UUID =
            Identifier.fromNamespaceAndPath(Mediumcore.MODID, "health_modifier");

    public static void registerAll(){
        onServerJoin();
        onDeath();
        onHealthReconcile();
    }

    public static void onHealthReconcile() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (!p.isAlive()) continue;
                if (!GameRuleRegistry.isMediumCoreMode(p.level().getGameRules())) continue;
                AttributeInstance attribute = p.getAttribute(Attributes.MAX_HEALTH);
                if (attribute == null) continue;

                double want = p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).orElseGet(CompoundTag::new).getDouble(HEALTH_MODIFIER_TAG).orElse(0.0D);
                AttributeModifier current = attribute.getModifier(HEALTH_MODIFIER_UUID);
                if (current == null || current.amount() != want) {
                    updateHealth(p, want);
                }
            }
        });
    }

    public static void onServerJoin() {
        ServerLifecycleEvents.SERVER_STARTED.register((handler) -> {
            // onEntityJoinLevel(ServerStartedEvent event)
            if (handler.isDedicatedServer() && handler instanceof DedicatedServer dedicatedServer) {
                boolean propertiesSayMediumcore = ((DedicatedServerPropertiesAccessor) dedicatedServer.getProperties()).isServerMediumcore();
                if (propertiesSayMediumcore) {
                    Mediumcore.LOGGER.info("set server game rule for mediumcore because it is set to true in server.properties");
                    for (ServerLevel level : handler.getAllLevels()) {
                        GameRuleRegistry.setMediumcoreMode(level.getGameRules(), propertiesSayMediumcore, handler);
                    }
                }
            }
        });
        ServerPlayerEvents.JOIN.register((handler) -> {
            if (!handler.level().isClientSide()) {
                boolean mediumcore = GameRuleRegistry.isMediumCoreMode(handler.level().getGameRules());
                if (mediumcore && handler instanceof ServerPlayer) {
                    Mediumcore.sendNonLocal(new SyncMediumcoreGameRuleMessage(mediumcore), handler);
                    CompoundTag tag = handler.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).orElseGet(CompoundTag::new);
                    double healthModifiedBy = tag.getDouble(HEALTH_MODIFIER_TAG).orElse(0.0D);
                    updateHealth(handler, healthModifiedBy);
                }
            }
        });
    }

    public static void onDeath(){
        ServerLivingEntityEvents.AFTER_DEATH.register(((entity, damageSource) -> {
            if (entity instanceof Player player && !player.level().isClientSide()) {
                if (player.level() instanceof ServerLevel slevel && GameRuleRegistry.isMediumCoreMode(slevel.getGameRules())) {
                    CompoundTag tag = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).orElseGet(CompoundTag::new);
                    double clampedHealth = Mth.clamp(player.getMaxHealth() - Mediumcore.CONFIG.healthDecreasePerDeath.get(), Mediumcore.CONFIG.minimumPlayerHealth.get(), Mediumcore.CONFIG.maxPlayerHealth.get());
                    double healthModifiedBy = tag.getDouble(HEALTH_MODIFIER_TAG).orElse(0.0D) + (clampedHealth - player.getMaxHealth());
                    tag.putDouble(HEALTH_MODIFIER_TAG, healthModifiedBy);
                    player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
                }
            }
        }));
    }


    /*
    public void onLivingFinishUsingItem() {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            if (event.getItem().is(MediumcoreTags.RESTORES_MAX_HEALTH) && GameRuleRegistry.isMediumCoreMode(event.getEntity().level().getGameRules())) {
                CompoundTag tag = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
                double clampedHealth = Mth.clamp(player.getMaxHealth() + Mediumcore.CONFIG.healthIncreasePerHeal.get(), Mediumcore.CONFIG.minimumPlayerHealth.get(), Mediumcore.CONFIG.maxPlayerHealth.get());
                double healthModifiedBy = tag.getDouble(HEALTH_MODIFIER_TAG) + (clampedHealth - player.getMaxHealth());
                tag.putDouble(HEALTH_MODIFIER_TAG, healthModifiedBy);
                player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
                updateHealth(player, healthModifiedBy);
            }
        }
    }
    Mixinnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn
     */

    public static void updateHealth(Player player, double healthModifiedBy) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null){
            return;
        }
        attribute.removeModifier(INITIAL_HEALTH_MODIFIER_UUID);
        attribute.addPermanentModifier(new AttributeModifier(INITIAL_HEALTH_MODIFIER_UUID, Mediumcore.CONFIG.startingPlayerHealth.get() - 20F, AttributeModifier.Operation.ADD_VALUE));
        attribute.removeModifier(HEALTH_MODIFIER_UUID);
        attribute.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_UUID, healthModifiedBy, AttributeModifier.Operation.ADD_VALUE));
        player.setHealth(Mth.clamp(player.getHealth(), 0, player.getMaxHealth()));
    }
}