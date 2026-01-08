package me.lagggpixel.replay.replay.content;

import me.lagggpixel.replay.api.replay.content.RecEntity;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class ReplayEntity extends RecEntity {

    public ReplayEntity(short id, UUID uuid, EntityType type, String name, float health) {
        super(id, uuid, type, name, health);
    }
}
