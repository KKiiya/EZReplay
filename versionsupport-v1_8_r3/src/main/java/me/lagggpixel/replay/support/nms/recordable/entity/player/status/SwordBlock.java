package me.lagggpixel.replay.support.nms.recordable.entity.player.status;

import lombok.Getter;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Player;

public class SwordBlock extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final byte value;

    public SwordBlock(IRecording replay, Player playerBlocking) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(playerBlocking.getUniqueId());
        this.value = (byte) (playerBlocking.isBlocking() ? 0x10 : 0x00);
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        EntityPlayer fakePlayer = (EntityPlayer) ((CraftEntity) replaySession.getSpawnedEntities().get(entityId)).getHandle();
        fakePlayer.getDataWatcher().watch(0, value);

        PacketPlayOutEntityMetadata playerMetadata = new PacketPlayOutEntityMetadata(fakePlayer.getId(), fakePlayer.getDataWatcher(), true);

        v1_8_R3.sendPacket(player, playerMetadata);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.SWORD_BLOCK;
    }
}
