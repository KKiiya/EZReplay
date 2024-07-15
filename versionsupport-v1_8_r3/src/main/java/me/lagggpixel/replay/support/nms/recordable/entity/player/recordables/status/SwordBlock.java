package me.lagggpixel.replay.support.nms.recordable.entity.player.recordables.status;

import lombok.Getter;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.player.recordables.status.IBlocking;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SwordBlock extends Recordable implements IBlocking {

    @Getter
    private final UUID uniqueId;
    private final boolean isBlocking;

    public SwordBlock(IRecording replay, Player playerBlocking) {
        super(replay);
        this.uniqueId = playerBlocking.getUniqueId();
        this.isBlocking = playerBlocking.isBlocking();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        EntityPlayer fakePlayer = (EntityPlayer) ((CraftEntity) replaySession.getSpawnedEntities().get(uniqueId.toString())).getHandle();

        byte blocking = (byte) (isBlocking ? 16 : 0);
        fakePlayer.getDataWatcher().watch(0, blocking);

        PacketPlayOutEntityMetadata playerMetadata = new PacketPlayOutEntityMetadata(fakePlayer.getId(), fakePlayer.getDataWatcher(), true);

        v1_8_R3.sendPacket(player, playerMetadata);
    }
}
