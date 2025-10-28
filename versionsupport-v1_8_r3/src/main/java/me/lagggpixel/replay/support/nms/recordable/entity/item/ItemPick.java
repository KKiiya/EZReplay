package me.lagggpixel.replay.support.nms.recordable.entity.item;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.PacketPlayOutCollect;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
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
    public void play(IReplaySession replaySession, Player player) {
        int subItemId = replaySession.getSpawnedEntities().get(itemId).getEntityId();
        int subCollectorId = replaySession.getSpawnedEntities().get(collectorId).getEntityId();

        PacketPlayOutCollect collect = new PacketPlayOutCollect(subItemId, subCollectorId);
        PacketPlayOutNamedSoundEffect pickUpSound = new PacketPlayOutNamedSoundEffect(CraftSound.getSound(Sound.ITEM_PICKUP), x, y, z, 0.7f, 1.4f);
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(itemId);

        v1_8_R3.sendPackets(player, collect, pickUpSound, destroy);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ITEM_PICK;
    }
}
