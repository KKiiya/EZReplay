package me.lagggpixel.replay.api.utils.entity;

import lombok.Getter;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import org.bukkit.entity.Entity;

import java.util.UUID;

@Getter
public class ReplayEntity {

    private final Entity associatedEntity;
    private final IReplaySession replaySession;
    private final int entityId;
    private final UUID uniqueId;

    public ReplayEntity(IReplaySession replaySession, Entity entity) {
        this.replaySession = replaySession;
        this.associatedEntity = entity;
        this.entityId = entity.getEntityId();
        this.uniqueId = entity.getUniqueId();
    }
}
