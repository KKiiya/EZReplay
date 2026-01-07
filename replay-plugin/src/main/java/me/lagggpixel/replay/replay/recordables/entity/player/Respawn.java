package me.lagggpixel.replay.replay.recordables.entity.player;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.utils.PacketUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Respawn extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final double x;
    @Writeable private final double y;
    @Writeable private final double z;
    @Writeable private final float yaw;
    @Writeable private final float pitch;

    public Respawn(IRecording replay, Player player) {
        super(replay);
        Location location = player.getLocation();
        this.entityId = replay.getEntityIndex().getOrRegister(player.getUniqueId());
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    @Override
    public void play(IReplaySession replaySession) {
        UUID fakePlayer = replaySession.getSpawnedEntities().get(entityId);
        Location spawnLocation = new Location(replaySession.getWorld(), x, y, z, yaw, pitch);
        PacketUtils.spawnFakePlayer(replaySession.getViewers(), fakePlayer, spawnLocation);
    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.RESPAWN;
    }
}
