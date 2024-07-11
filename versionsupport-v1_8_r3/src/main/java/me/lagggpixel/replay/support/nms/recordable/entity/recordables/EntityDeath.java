package me.lagggpixel.replay.support.nms.recordable.entity.recordables;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.recordables.IEntityDeath;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EntityDeath extends Recordable implements IEntityDeath {

    private final EntityType type;
    private final UUID uniqueId;
    private final int entityId;

    public EntityDeath(IRecording replay, Entity entity) {
        super(replay);
        this.type = entity.getType();
        this.uniqueId = entity.getUniqueId();
        this.entityId = entity.getEntityId();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {

    }

    @Override
    public EntityType getType() {
        return type;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
}
