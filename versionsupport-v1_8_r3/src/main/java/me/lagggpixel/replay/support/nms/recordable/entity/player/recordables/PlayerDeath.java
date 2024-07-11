package me.lagggpixel.replay.support.nms.recordable.entity.player.recordables;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.support.nms.recordable.entity.recordables.EntityDeath;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerDeath extends EntityDeath {

    public PlayerDeath(IRecording replay, Player player) {
        super(replay, player);
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        Entity fakePlayer = ((CraftPlayer) replaySession.getFakePlayer(getUniqueId().toString()).getEntity()).getHandle();
        DataWatcher.WatchableObject dyingWatcher = new DataWatcher.WatchableObject(6, 20, 7);
        fakePlayer.getDataWatcher().c().add(dyingWatcher);

        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(fakePlayer.getId(), fakePlayer.getDataWatcher(), true);

        v1_8_R3.sendPacket(player, metadata);
    }
}
