package com.github.alexmodguy.mediumcore.event;

import com.github.alexmodguy.mediumcore.GameRuleRegistry;
import com.github.alexmodguy.mediumcore.Mediumcore;
import com.github.alexmodguy.mediumcore.MediumcoreTags;
import com.github.alexmodguy.mediumcore.misc.DedicatedServerPropertiesAccessor;
import com.github.alexmodguy.mediumcore.packet.SyncMediumcoreGameRuleMessage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
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

    private static final String HEALTH_MODIFIER_TAG = "MediumcoreHealthModifier";
    private static final Identifier INITIAL_HEALTH_MODIFIER_UUID =
            Identifier.fromNamespaceAndPath(Mediumcore.MODID, "initial_health_modifier");
    private static final Identifier HEALTH_MODIFIER_UUID =
            Identifier.fromNamespaceAndPath(Mediumcore.MODID, "health_modifier");

    @SubscribeEvent
    public void onEntityJoinLevel(ServerStartedEvent event) {
        if(event.getServer().isDedicatedServer() && event.getServer() instanceof DedicatedServer dedicatedServer){
            boolean propertiesSayMediumcore = ((DedicatedServerPropertiesAccessor)dedicatedServer.getProperties()).isServerMediumcore();
            if(propertiesSayMediumcore){
                Mediumcore.LOGGER.info("set server game rule for mediumcore because it is set to true in server.properties");
                GameRuleRegistry.setMediumcoreMode(event.getServer().getGameRules(), propertiesSayMediumcore, event.getServer());
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player && !event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            boolean mediumcore = GameRuleRegistry.isMediumCoreMode(serverLevel.getGameRules());
            if (mediumcore && player instanceof ServerPlayer) {
                Mediumcore.sendNonLocal(new SyncMediumcoreGameRuleMessage(mediumcore), (ServerPlayer) player);
                CompoundTag tag = event.getEntity().getPersistentData();
                double healthModifiedBy = tag.getDouble(HEALTH_MODIFIER_TAG).orElse(0.0D);
                updateHealth(player, healthModifiedBy);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel) {
            if (GameRuleRegistry.isMediumCoreMode(serverLevel.getGameRules())) {
                CompoundTag tag = player.getPersistentData();
                double clampedHealth = Mth.clamp(player.getMaxHealth() - Mediumcore.CONFIG.healthDecreasePerDeath.get(), Mediumcore.CONFIG.minimumPlayerHealth.get(), Mediumcore.CONFIG.maxPlayerHealth.get());
                double healthModifiedBy = tag.getDouble(HEALTH_MODIFIER_TAG).orElse(0.0D) + (clampedHealth - player.getMaxHealth());
                tag.putDouble(HEALTH_MODIFIER_TAG, healthModifiedBy);
                player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.isEndConquered() && !event.getEntity().level().isClientSide() && event.getEntity().level() instanceof ServerLevel serverLevel) {
            if (GameRuleRegistry.isMediumCoreMode(serverLevel.getGameRules())) {
                CompoundTag tag = event.getEntity().getPersistentData();
                double healthModifiedBy = tag.getDouble(HEALTH_MODIFIER_TAG).orElse(0.0D);
                updateHealth(event.getEntity(), healthModifiedBy);
            }
        }
    }

    @SubscribeEvent
    public void onLivingFinishUsingItem(LivingEntityUseItemEvent.Finish event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide() && event.getEntity().level() instanceof ServerLevel serverLevel) {
            if (event.getItem().is(MediumcoreTags.RESTORES_MAX_HEALTH) && GameRuleRegistry.isMediumCoreMode(serverLevel.getGameRules())) {
                CompoundTag tag = player.getPersistentData();
                double clampedHealth = Mth.clamp(player.getMaxHealth() + Mediumcore.CONFIG.healthIncreasePerHeal.get(), Mediumcore.CONFIG.minimumPlayerHealth.get(), Mediumcore.CONFIG.maxPlayerHealth.get());
                double healthModifiedBy = tag.getDouble(HEALTH_MODIFIER_TAG).orElse(0.0D) + (clampedHealth - player.getMaxHealth());
                tag.putDouble(HEALTH_MODIFIER_TAG, healthModifiedBy);
                player.getPersistentData().put(Player.PERSISTED_NBT_TAG, tag);
                updateHealth(player, healthModifiedBy);
            }
        }
    }

    private void updateHealth(Player player, double healthModifiedBy) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_HEALTH);
        attribute.removeModifier(INITIAL_HEALTH_MODIFIER_UUID);
        attribute.addPermanentModifier(new AttributeModifier(INITIAL_HEALTH_MODIFIER_UUID, Mediumcore.CONFIG.startingPlayerHealth.get() - 20F, AttributeModifier.Operation.ADD_VALUE));
        attribute.removeModifier(HEALTH_MODIFIER_UUID);
        attribute.addPermanentModifier(new AttributeModifier(HEALTH_MODIFIER_UUID, healthModifiedBy, AttributeModifier.Operation.ADD_VALUE));
        player.setHealth(Mth.clamp(player.getHealth(), 0, player.getMaxHealth()));
    }
}