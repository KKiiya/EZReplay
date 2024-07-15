package me.lagggpixel.replay.api.replay.data.recordable.entity;

import org.bukkit.entity.EntityType;

import java.util.UUID;

public interface IEntityRecordable {
    EntityType getType();
    UUID getUniqueId();
}
