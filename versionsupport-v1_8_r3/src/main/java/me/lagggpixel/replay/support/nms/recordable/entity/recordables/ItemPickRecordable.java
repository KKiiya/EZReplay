package me.lagggpixel.replay.support.nms.recordable.entity.recordables;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.PacketPlayOutCollect;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class ItemPickRecordable extends Recordable {

    private final String itemUUID;
    private final String collectorUUID;

    public ItemPickRecordable(IRecording replay, Item item, Entity collector) {
        super(replay);
        this.itemUUID = item.getUniqueId().toString();
        this.collectorUUID = collector.getUniqueId().toString();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        int itemId = replaySession.getSpawnedEntities().get(itemUUID).getEntityId();
        int collectorId = replaySession.getSpawnedEntities().get(collectorUUID).getEntityId();

        PacketPlayOutCollect collect = new PacketPlayOutCollect(itemId, collectorId);
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(itemId);

        v1_8_R3.sendPackets(player, collect, destroy);
    }
}
