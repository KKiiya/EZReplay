package me.lagggpixel.replay.support.nms.recordable.entity.item;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
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

    @Writeable
    private final String itemUUID;
    @Writeable
    private final String collectorUUID;
    @Writeable
    private final double x;
    @Writeable
    private final double y;
    @Writeable
    private final double z;

    public ItemPick(IRecording replay, Item item, Entity collector) {
        super(replay);
        this.itemUUID = item.getUniqueId().toString();
        this.collectorUUID = collector.getUniqueId().toString();
        this.x = item.getLocation().getX();
        this.y = item.getLocation().getY();
        this.z = item.getLocation().getZ();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        int itemId = replaySession.getSpawnedEntities().get(itemUUID).getEntityId();
        int collectorId = replaySession.getSpawnedEntities().get(collectorUUID).getEntityId();

        PacketPlayOutCollect collect = new PacketPlayOutCollect(itemId, collectorId);
        PacketPlayOutNamedSoundEffect pickUpSound = new PacketPlayOutNamedSoundEffect(CraftSound.getSound(Sound.ITEM_PICKUP), x, y, z, 0.7f, 1.4f);
        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(itemId);

        v1_8_R3.sendPackets(player, collect, pickUpSound, destroy);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }
}
