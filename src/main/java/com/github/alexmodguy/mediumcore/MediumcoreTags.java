package com.github.alexmodguy.mediumcore;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class MediumcoreTags {
    public static final TagKey<Item> RESTORES_MAX_HEALTH = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("mediumcore","restores_max_health"));
}