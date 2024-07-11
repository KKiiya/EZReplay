package me.lagggpixel.replay.api.replay.data.recordable.entity.player;

import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public interface IPlayerRecordable {

    UUID getUniqueId();

    int getEntityId();
}
