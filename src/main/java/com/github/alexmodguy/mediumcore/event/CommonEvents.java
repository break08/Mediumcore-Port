package com.github.alexmodguy.mediumcore.event;

import com.github.alexmodguy.mediumcore.FabricIsSoBadAtDataAttach;
import com.github.alexmodguy.mediumcore.GameRuleRegistry;
import com.github.alexmodguy.mediumcore.Mediumcore;
import com.github.alexmodguy.mediumcore.MediumcoreTags;
import com.github.alexmodguy.mediumcore.misc.DedicatedServerPropertiesAccessor;
import com.github.alexmodguy.mediumcore.packet.SyncMediumcoreGameRuleMessage;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.core.jmx.Server;
/*
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
/*
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

 */

import java.util.UUID;

public class CommonEvents {

    public static final String HEALTH_MODIFIER_TAG = "MediumcoreHealthModifier";
    private static final ResourceLocation INITIAL_HEALTH_MODIFIER_UUID =
            ResourceLocation.fromNamespaceAndPath(Mediumcore.MODID, "initial_health_modifier");
    private static final ResourceLocation HEALTH_MODIFIER_UUID =
            ResourceLocation.fromNamespaceAndPath(Mediumcore.MODID, "health_modifier");

    public static void registerAll(){
        onServerJoin();
        onDeath();
        onPlayerRespawn();
    }

    public static void onServerJoin() {
        ServerPlayerEvents.JOIN.register((handler) -> {
            // onEntityJoinLevel(ServerStartedEvent event)
            MinecraftServer server = handler.getServer();
            if (server == null){
                return;
            }
            if (server.isDedicatedServer() && server instanceof DedicatedServer dedicatedServer) {
                boolean propertiesSayMediumcore = ((DedicatedServerPropertiesAccessor) dedicatedServer.getProperties()).isServerMediumcore();
                if (propertiesSayMediumcore) {
                    Mediumcore.LOGGER.info("set server game rule for mediumcore because it is set to true in server.properties");
                    GameRuleRegistry.setMediumcoreMode(server.getGameRules(), propertiesSayMediumcore, server);
                }
            }
        });
        ServerPlayerEvents.JOIN.register((handler) -> {
            if (handler instanceof Player player && !handler.serverLevel().isClientSide) {
                boolean mediumcore = GameRuleRegistry.isMediumCoreMode(handler.serverLevel().getGameRules());
                if (mediumcore && player instanceof ServerPlayer) {
                    Mediumcore.sendNonLocal(new SyncMediumcoreGameRuleMessage(mediumcore), (ServerPlayer) player);
                    CompoundTag tag = FabricIsSoBadAtDataAttach.getPersisted(handler);
                    double healthModifiedBy = tag.getDouble(HEALTH_MODIFIER_TAG);
                    updateHealth(player, healthModifiedBy);
                }
            }
        });
    }

    public static void onDeath(){
        ServerLivingEntityEvents.AFTER_DEATH.register(((entity, damageSource) -> {
            if (entity instanceof Player player && !player.level().isClientSide) {
                if (GameRuleRegistry.isMediumCoreMode(player.level().getGameRules())) {
                    CompoundTag tag = FabricIsSoBadAtDataAttach.getPersisted(player);
                    double clampedHealth = Mth.clamp(player.getMaxHealth() - Mediumcore.CONFIG.healthDecreasePerDeath.get(), Mediumcore.CONFIG.minimumPlayerHealth.get(), Mediumcore.CONFIG.maxPlayerHealth.get());
                    double healthModifiedBy = tag.getDouble(HEALTH_MODIFIER_TAG) + (clampedHealth - player.getMaxHealth());
                    tag.putDouble(HEALTH_MODIFIER_TAG, healthModifiedBy);
                    FabricIsSoBadAtDataAttach.getPersistentData(player).put(FabricIsSoBadAtDataAttach.PERSISTED_NBT_TAG, tag);
                }
            }
        }));
    }


    public static void onPlayerRespawn() {
        ServerPlayerEvents.AFTER_RESPAWN.register((pOld, pNew, realDeath) -> {
            if (!realDeath && !pNew.level().isClientSide) {
                if (GameRuleRegistry.isMediumCoreMode(pNew.level().getGameRules())) {
                    CompoundTag tag = FabricIsSoBadAtDataAttach.getPersisted(pNew);
                    double healthModifiedBy = tag.getDouble(HEALTH_MODIFIER_TAG);
                    updateHealth(pNew, healthModifiedBy);
                }
            }
        });
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
        attribute.removeModifier(INITIAL_HEALTH_MODIFIER_UUID);
        attribute.addPermanentModifier(new AttributeModifier(INITIAL_HEALTH_MODIFIER_UUID, Mediumcore.CONFIG.startingPlayerHealth.get() - 20F, AttributeModifier.Operation.ADD_VALUE));
        attribute.removeModifier(HEALTH_MODIFIER_UUID);
        attribute.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_UUID, healthModifiedBy, AttributeModifier.Operation.ADD_VALUE));
        player.setHealth(Mth.clamp(player.getHealth(), 0, player.getMaxHealth()));
    }
}