package me.lagggpixel.replay.support.nms.recordable.entity;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.IEntityRecordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import org.bukkit.Location;
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

    private final UUID uniqueId;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public EntityRecordable(IRecording replay, Entity entity) {
        super(replay);
        this.uniqueId = entity.getUniqueId();
        Location location = entity.getLocation();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        Entity replayEntity = replaySession.getSpawnedEntities().get(getUniqueId().toString());
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftEntity) replayEntity).getHandle();

        entity.setPositionRotation(x, y, z, yaw, pitch);

        PacketPlayOutEntityTeleport positionPacket = new PacketPlayOutEntityTeleport(entity);
        PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(entity, (byte) ((yaw * 256.0F) / 360.0F));
        PacketPlayOutEntity.PacketPlayOutEntityLook entityLook = new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getId(), (byte) ((yaw * 256.0F) / 360.0F), (byte) ((pitch * 256.0F) / 360.0F), true);

        v1_8_R3.sendPackets(player, positionPacket, headRotation, entityLook);
    }

    @Override
    public EntityType getType() {
        return null;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }
}
