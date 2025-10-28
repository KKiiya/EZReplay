package me.lagggpixel.replay.support.nms.recordable.entity.status;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Player;

public class Burning extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final int fireTicks;

    public Burning(IRecording replay, org.bukkit.entity.Entity entity) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
        this.fireTicks = entity.getFireTicks();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        Entity fakeEntity = ((CraftEntity) replaySession.getSpawnedEntities().get(entityId)).getHandle();
        fakeEntity.setOnFire(fireTicks);

        PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata(fakeEntity.getId(), fakeEntity.getDataWatcher(), true);

        v1_8_R3.sendPacket(player, entityMetadata);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.BURNING;
    }
}
