package me.lagggpixel.replay.support.nms.recordable.entity.recordables;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.recordables.IEntityDeath;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import java.util.UUID;

public class EntityDeath extends Recordable implements IEntityDeath {

    private final EntityType type;
    private final UUID uniqueId;

    public EntityDeath(IRecording replay, Entity entity) {
        super(replay);
        this.type = entity.getType();
        this.uniqueId = entity.getUniqueId();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        net.minecraft.server.v1_8_R3.Entity fakeEntity = ((CraftEntity) replaySession.getSpawnedEntities().get(uniqueId.toString())).getHandle();
        fakeEntity.dead = true;

        PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus(fakeEntity, (byte) 3);
        v1_8_R3.sendPacket(player, status);

        Bukkit.getScheduler().runTaskLater(v1_8_R3.getInstance().getPlugin(), () -> {
            PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(fakeEntity.getId());
            v1_8_R3.sendPacket(player, destroy);
        }, 20L);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        net.minecraft.server.v1_8_R3.Entity fakeEntity = ((CraftEntity) replaySession.getSpawnedEntities().get(uniqueId.toString())).getHandle();
        fakeEntity.dead = false;

        PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving((EntityLiving) fakeEntity);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(fakeEntity.getId(), fakeEntity.getDataWatcher(), true);

        v1_8_R3.sendPackets(player, spawn, metadata);
    }
}
