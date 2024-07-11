package me.lagggpixel.replay.support.nms.recordable.entity.recordables;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.recordables.IEntityStatus;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.entity.ReplayEntity;
import me.lagggpixel.replay.api.utils.information.IEntityInformation;
import me.lagggpixel.replay.support.nms.utils.information.EntityInformation;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public class EntityStatus extends Recordable implements IEntityStatus {
    private final IEntityInformation entityInformation;

    public EntityStatus(IRecording replay, Entity entity) {
        super(replay);
        this.entityInformation = new EntityInformation(entity);
    }

    @Override
    public EntityType getType() {
        return null;
    }

    @Override
    public int getEntityId() {
        return entityInformation.getEntityId();
    }

    @Override
    public UUID getUniqueId() {
        return entityInformation.getUniqueId();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        ReplayEntity replayEntity = replaySession.getFakeEntity(getEntityId());
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftEntity) replayEntity.getAssociatedEntity()).getHandle();
        int entityId = replayEntity.getEntityId();

        entity.setOnFire(entityInformation.getFireTicks());

        PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata(entityId, entity.getDataWatcher(), true);

        v1_8_R3.sendPacket(player, entityMetadata);
    }
}
