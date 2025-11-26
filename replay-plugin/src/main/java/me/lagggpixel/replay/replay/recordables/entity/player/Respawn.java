package me.lagggpixel.replay.replay.recordables.entity.player;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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
    public void play(IReplaySession replaySession, Player player) {
        Player fakePlayer = (Player) replaySession.getSpawnedEntities().get(entityId);
        Location spawnLocation = new Location(replaySession.getWorld(), x, y, z, yaw, pitch);
        v1_8_R3.getInstance().spawnFakePlayer(fakePlayer, player, spawnLocation);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.RESPAWN;
    }
}
