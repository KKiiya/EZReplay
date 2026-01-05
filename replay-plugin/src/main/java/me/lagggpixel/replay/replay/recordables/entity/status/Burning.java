package me.lagggpixel.replay.replay.recordables.entity.status;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Burning extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final int fireTicks;

    public Burning(IRecording replay, Entity entity) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
        this.fireTicks = entity.getFireTicks();
    }

    @Override
    public void play(IReplaySession replaySession) {
        Entity fakeEntity = ((CraftEntity) replaySession.getSpawnedEntities().get(entityId)).getHandle();
        fakeEntity.setOnFire(fireTicks);

        PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata(fakeEntity.getId(), fakeEntity.getDataWatcher(), true);

        v1_8_R3.sendPacket(player, entityMetadata);
    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.BURNING;
    }
}
