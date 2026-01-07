package me.lagggpixel.replay.replay.content;

import java.util.UUID;
import org.bukkit.entity.EntityType;
import me.lagggpixel.replay.api.replay.content.RecEntity;

public class ReplayEntity extends RecEntity {

    public ReplayEntity(short id, UUID uuid, EntityType type, String name, float health) {
        super(id, uuid, type, name, health);
    }
}
