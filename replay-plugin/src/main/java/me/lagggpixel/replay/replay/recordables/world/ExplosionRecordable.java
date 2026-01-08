package me.lagggpixel.replay.replay.recordables.world;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.content.RecEntity;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import me.lagggpixel.replay.utils.SerializerUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.FLOAT;
import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.SHORT;

public class ExplosionRecordable extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final me.lagggpixel.replay.api.utils.Vector3d position;
    @Writeable private final float strength;

    public ExplosionRecordable(ReplayByteBuffer reader) {
        super(null);
        this.entityId = reader.read(SHORT);
        this.position = SerializerUtil.read3DVector(reader);
        this.strength = reader.read(FLOAT);
    }

    public ExplosionRecordable(IRecording replay, Location location, Entity entity, float radius) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
        this.position = me.lagggpixel.replay.api.utils.Vector3d.fromBukkitLocation(location);
        this.strength = radius;
    }

    @Override
    public void play(IReplaySession replaySession) {
        RecEntity tnt = replaySession.getSpawnedEntities().get(entityId);
        int x = (int) position.getX();
        int y = (int) position.getY();
        int z = (int) position.getZ();

        WrapperPlayServerExplosion explosionPacket = new WrapperPlayServerExplosion(new Vector3d(x, y, z), strength, new ArrayList<>(), new Vector3f(0, 0, 0));
        WrapperPlayServerSoundEffect soundPacket = new WrapperPlayServerSoundEffect(Sounds.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCK, new Vector3i(x, y, z), 1.0f, 0.8f);

        WrapperPlayServerDestroyEntities destroyPacket = null;
        if (tnt != null) {
            int fakeEntityId = entityId + 100000;
            destroyPacket = new WrapperPlayServerDestroyEntities(fakeEntityId);
        }

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);

            user.sendPacket(explosionPacket);
            user.sendPacket(soundPacket);
            if (destroyPacket != null) user.sendPacket(destroyPacket);
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.EXPLOSION;
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(SHORT, entityId);
        SerializerUtil.write3DVector(writer, position);
        writer.write(FLOAT, strength);
    }
}
