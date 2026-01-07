package me.lagggpixel.replay.replay.recordables.entity.item;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.utils.item.ItemData;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class ItemDrop extends Recordable {

    @Writeable private final ItemData data;
    @Writeable private final me.lagggpixel.replay.api.utils.Vector3d location;
    @Writeable private final double motX;
    @Writeable private final double motY;
    @Writeable private final double motZ;
    @Writeable private final short entityId;

    public ItemDrop(IRecording replay, Item item) {
        super(replay);
        this.location = me.lagggpixel.replay.api.utils.Vector3d.fromBukkitLocation(item.getLocation());
        this.entityId = replay.getEntityIndex().getOrRegister(item.getUniqueId());
        this.motX = item.getVelocity().getX();
        this.motY = item.getVelocity().getY();
        this.motZ = item.getVelocity().getZ();
        this.data = new ItemData(item.getItemStack());
    }

    @Override
    public void play(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000;
        Vector3d position = new Vector3d(location.getX(), location.getY(), location.getZ());

        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(fakeEntityId, Optional.of(UUID.randomUUID()), EntityTypes.ITEM, position, 0f, 0f, 0f, 0, Optional.of(new Vector3d(motX, motY, motZ)));
        WrapperPlayServerEntityVelocity velocityPacket = new WrapperPlayServerEntityVelocity(fakeEntityId, new Vector3d(motX, motY, motZ));

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(spawnPacket);
            user.sendPacket(velocityPacket);
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000;

        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(fakeEntityId);

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(destroyPacket);
        }
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ITEM_DROP;
    }
}
