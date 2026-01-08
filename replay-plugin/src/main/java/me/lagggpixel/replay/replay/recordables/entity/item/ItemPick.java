package me.lagggpixel.replay.replay.recordables.entity.item;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerCollectItem;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSoundEffect;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.DOUBLE;
import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.SHORT;

public class ItemPick extends Recordable {

    @Writeable private final short itemId;
    @Writeable private final short collectorId;
    @Writeable private final double x;
    @Writeable private final double y;
    @Writeable private final double z;

    public ItemPick(ReplayByteBuffer reader) {
        super(null);
        this.itemId = reader.read(SHORT);
        this.collectorId = reader.read(SHORT);
        this.x = reader.read(DOUBLE);
        this.y = reader.read(DOUBLE);
        this.z = reader.read(DOUBLE);
    }

    public ItemPick(IRecording replay, Item item, Entity collector) {
        super(replay);
        this.itemId = replay.getEntityIndex().getOrRegister(item.getUniqueId());
        this.collectorId = replay.getEntityIndex().getOrRegister(collector.getUniqueId());
        this.x = item.getLocation().getX();
        this.y = item.getLocation().getY();
        this.z = item.getLocation().getZ();
    }

    @Override
    public void play(IReplaySession replaySession) {
        int fakeItemId = itemId + 100000;
        int fakeCollectorId = collectorId + 100000;

        WrapperPlayServerCollectItem collectPacket = new WrapperPlayServerCollectItem(fakeItemId, fakeCollectorId, 1);
        WrapperPlayServerSoundEffect soundPacket = new WrapperPlayServerSoundEffect(Sounds.ENTITY_ITEM_PICKUP, SoundCategory.PLAYER, new Vector3i((int) x, (int) y, (int) z), 0.7f, 1.4f);
        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(fakeItemId);

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(collectPacket);
            user.sendPacket(soundPacket);
            user.sendPacket(destroyPacket);
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ITEM_PICK;
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(SHORT, itemId);
        writer.write(SHORT, collectorId);
        writer.write(DOUBLE, x);
        writer.write(DOUBLE, y);
        writer.write(DOUBLE, z);
    }
}
