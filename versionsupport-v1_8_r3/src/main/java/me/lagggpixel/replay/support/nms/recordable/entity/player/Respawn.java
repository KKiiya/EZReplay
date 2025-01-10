package me.lagggpixel.replay.support.nms.recordable.entity.player;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Respawn extends Recordable {

    private final String uuid;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;

    public Respawn(IRecording replay, Player player) {
        super(replay);
        Location location = player.getLocation();
        this.uuid = player.getUniqueId().toString();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        Player fakePlayer = (Player) replaySession.getSpawnedEntities().get(uuid);
        Location spawnLocation = new Location(replaySession.getWorld(), x, y, z, yaw, pitch);
        v1_8_R3.getInstance().spawnFakePlayer(fakePlayer, player, spawnLocation);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }
}
