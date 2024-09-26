package me.lagggpixel.replay.support.nms.recordable.entity.player.recordables.status;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Sprinting extends Recordable {

    private final UUID uniqueId;
    private final boolean isSprinting;

    public Sprinting(IRecording replay, UUID player, boolean isSprinting) {
        super(replay);
        this.uniqueId = player;
        this.isSprinting = isSprinting;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        EntityPlayer fakePlayer = (EntityPlayer) ((CraftEntity) replaySession.getSpawnedEntities().get(uniqueId.toString())).getHandle();

        fakePlayer.setSprinting(isSprinting);

        PacketPlayOutEntityMetadata playerMetadata = new PacketPlayOutEntityMetadata(fakePlayer.getId(), fakePlayer.getDataWatcher(), true);

        v1_8_R3.sendPacket(player, playerMetadata);
    }
}
