package me.lagggpixel.replay.replay.recordables.entity.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.content.RecEntity;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import me.lagggpixel.replay.api.utils.Vector3d;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.INT;
import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.SHORT;

public class EntityDeath extends Recordable {

    @Writeable private final EntityType type;
    @Writeable private final short entityId;

    public EntityDeath(ReplayByteBuffer reader) {
        super(null);
        this.type = EntityType.values()[reader.read(INT)];
        this.entityId = reader.read(SHORT);
    }

    public EntityDeath(IRecording replay, Entity entity) {
        super(replay);
        this.type = entity.getType();
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
    }

    @Override
    public void play(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000;
        RecEntity recEntity = replaySession.getSpawnedEntities().get(entityId);
        Vector3d position = recEntity != null ? recEntity.getPosition() : new Vector3d(0, 0, 0);
        
        WrapperPlayServerEntityStatus statusPacket = new WrapperPlayServerEntityStatus(fakeEntityId, 3);
        WrapperPlayServerSoundEffect soundPacket = new WrapperPlayServerSoundEffect(getDeathSound(type), SoundCategory.AMBIENT, new Vector3i((int) position.getX(), (int) position.getY(), (int) position.getZ()), 1.0f, 1.0f);
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(fakeEntityId);
        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);

            // Send entity death status (3 = entity death)
            user.sendPacket(statusPacket);
            user.sendPacket(soundPacket);
            
            // Schedule destroy packet after a short delay
            Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> {
                User userLater = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
                userLater.sendPacket(destroyPacket);
            }, 20L);
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {
        // Respawn is complex and would require re-spawning the entity
        // This is left as a TODO or handled by other recordables
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ENTITY_DEATH;
    }

    private Sound getDeathSound(EntityType type) {
        return Sounds.getByName("entity." + type.name().toLowerCase() + ".death");
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(INT, type.ordinal());
        writer.write(SHORT, entityId);
    }
}
