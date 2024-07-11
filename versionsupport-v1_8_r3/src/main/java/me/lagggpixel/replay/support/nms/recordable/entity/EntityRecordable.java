package me.lagggpixel.replay.support.nms.recordable.entity;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.IEntityRecordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.utils.Camera;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.api.utils.entity.ReplayEntity;
import me.lagggpixel.replay.api.utils.information.IEntityInformation;
import me.lagggpixel.replay.support.nms.utils.information.EntityInformation;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public class EntityRecordable extends Recordable implements IEntityRecordable {
    private final IEntityInformation entityInformation;

    public EntityRecordable(IRecording replay, Entity entity) {
        super(replay);
        this.entityInformation = new EntityInformation(entity);
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        ReplayEntity replayEntity = replaySession.getFakeEntity(getEntityId());
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftEntity) replayEntity.getAssociatedEntity()).getHandle();

        Vector3d newPosition = entityInformation.getLocation();
        Camera newCamera = entityInformation.getCamera();

        double newX = newPosition.getX();
        double newY = newPosition.getY();
        double newZ = newPosition.getZ();

        float newYaw = newCamera.getYaw();
        float newPitch = newCamera.getPitch();

        entity.setPositionRotation(newX, newY, newZ, newYaw, newPitch);

        PacketPlayOutEntityTeleport positionPacket = new PacketPlayOutEntityTeleport(entity);

        v1_8_R3.sendPacket(player, positionPacket);
    }

    @Override
    public EntityType getType() {
        return null;
    }

    @Override
    public int getEntityId() {
        return 0;
    }

    @Override
    public UUID getUniqueId() {
        return null;
    }
}
