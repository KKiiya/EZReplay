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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class ItemPick extends Recordable {

    @Writeable private final short itemId;
    @Writeable private final short collectorId;
    @Writeable private final double x;
    @Writeable private final double y;
    @Writeable private final double z;

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
}
