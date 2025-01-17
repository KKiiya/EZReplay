package me.lagggpixel.replay.support.nms.recordable.entity.player.status;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Invisible extends Recordable {

    @Writeable
    private final UUID uniqueId;
    @Writeable
    private final boolean isInvisible;

    public Invisible(IRecording replay, Player player, boolean isInvisible) {
        super(replay);
        this.uniqueId = player.getUniqueId();
        this.isInvisible = isInvisible;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        EntityPlayer fakePlayer = (EntityPlayer) ((CraftEntity) replaySession.getSpawnedEntities().get(uniqueId.toString())).getHandle();
        fakePlayer.setInvisible(isInvisible);

        PacketPlayOutEntityMetadata playerMetadata = new PacketPlayOutEntityMetadata(fakePlayer.getId(), fakePlayer.getDataWatcher(), true);

        v1_8_R3.sendPacket(player, playerMetadata);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        EntityPlayer fakePlayer = (EntityPlayer) ((CraftEntity) replaySession.getSpawnedEntities().get(uniqueId.toString())).getHandle();
        fakePlayer.setInvisible(!isInvisible);

        PacketPlayOutEntityMetadata playerMetadata = new PacketPlayOutEntityMetadata(fakePlayer.getId(), fakePlayer.getDataWatcher(), true);

        v1_8_R3.sendPacket(player, playerMetadata);
    }
}
