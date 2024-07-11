package me.lagggpixel.replay.support.nms.recordable.entity.recordables;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.recordables.IEntitySpawn;
import me.lagggpixel.replay.api.utils.entity.ReplayEntity;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import java.util.UUID;

public class EntitySpawn extends Recordable implements IEntitySpawn {

    private final EntityType entityType;
    private final Location spawnLocation;
    private final int entityId;
    private final UUID uniqueId;

    public EntitySpawn(IRecording replay, Location spawnLocation, EntityType entityType, int entityId, UUID uniqueId) {
        super(replay);
        this.spawnLocation = spawnLocation;
        this.entityType = entityType;
        this.entityId = entityId;
        this.uniqueId = uniqueId;
    }

    @Override
    public EntityType getType() {
        return entityType;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        CraftWorld world = ((CraftWorld) replaySession);
        net.minecraft.server.v1_8_R3.Entity entity = world.createEntity(spawnLocation, entityType.getEntityClass());

        ReplayEntity replayEntity = new ReplayEntity(replaySession, entity.getBukkitEntity());
        replaySession.addFakeEntity(replayEntity);

        PacketPlayOutSpawnEntity entitySpawn = new PacketPlayOutSpawnEntity(entity, entityType.getTypeId());

        v1_8_R3.sendPacket(player, entitySpawn);
    }
}
