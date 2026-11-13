package com.github.alexmodguy.mediumcore;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class FabricIsSoBadAtDataAttach {
    public static final String PERSISTED_NBT_TAG = "PlayerPersisted";

    private static final AttachmentType<CompoundTag> TYPE = AttachmentRegistry.create(
            ResourceLocation.fromNamespaceAndPath("mediumcore", "persistent_data"),
            builder -> builder
                    .initializer(CompoundTag::new)
                    .persistent(CompoundTag.CODEC)
                    .copyOnDeath()
    );

    public static CompoundTag get(Entity entity) {
        return entity.getAttachedOrCreate(TYPE);
    }

    public static CompoundTag getPersisted(Entity entity) {
        CompoundTag root = get(entity);
        if (!root.contains(PERSISTED_NBT_TAG)) {
            root.put(PERSISTED_NBT_TAG, new CompoundTag());
        }
        return root.getCompound(PERSISTED_NBT_TAG);
    }

    public static CompoundTag getPersistentData(Entity entity) {
        return entity.getAttachedOrCreate(TYPE);
    }
}
