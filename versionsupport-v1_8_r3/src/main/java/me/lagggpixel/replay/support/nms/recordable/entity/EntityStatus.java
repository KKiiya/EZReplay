package me.lagggpixel.replay.support.nms.recordable.entity;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class EntityStatus extends Recordable {

    private final UUID uuid;
    private final boolean isDead;

    public EntityStatus(IRecording replay, Entity entity) {
        super(replay);
        this.uuid = entity.getUniqueId();
        this.isDead = entity.isDead();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        Entity entity = replaySession.getSpawnedEntities().get(uuid.toString());
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) entity).getHandle();

        if (isDead) {
            PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(nmsEntity.getId());
            nmsEntity.dead = true;
            v1_8_R3.sendPacket(player, destroy);
        }
    }
}
