package me.lagggpixel.replay.api.replay.content;

import java.util.UUID;
import org.bukkit.entity.EntityType;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.utils.Vector3d;

public abstract class RecEntity {
    @Writeable private final short id;
    @Writeable private final UUID uuid;
    @Writeable private final EntityType type;
    @Writeable private final String name;
    private float health;
    private Vector3d position;

    public RecEntity(short id, UUID uuid, EntityType type, String name, float health) {
        this.id = id;
        this.uuid = uuid;
        this.type = type;
        this.name = name;
        this.health = health;
        this.position = new Vector3d(0, 0, 0);
    }

    public short getFakeId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public EntityType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public Vector3d getPosition() {
        return position;
    }

    public void setPosition(Vector3d position) {
        this.position = position;
    }
}
