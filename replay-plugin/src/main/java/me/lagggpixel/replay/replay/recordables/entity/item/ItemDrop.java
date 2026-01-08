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
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import me.lagggpixel.replay.api.utils.item.ItemData;
import me.lagggpixel.replay.utils.SerializerUtil;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.*;

public class ItemDrop extends Recordable {

    @Writeable private final ItemData data;
    @Writeable private final me.lagggpixel.replay.api.utils.Vector3d location;
    @Writeable private final double motX;
    @Writeable private final double motY;
    @Writeable private final double motZ;
    @Writeable private final short entityId;

    public ItemDrop(ReplayByteBuffer reader) {
        super(null);
        this.entityId = reader.read(SHORT);
        this.location = SerializerUtil.read3DVector(reader);
        this.motX = reader.read(DOUBLE);
        this.motY = reader.read(DOUBLE);
        this.motZ = reader.read(DOUBLE);
        Material material = Material.values()[reader.read(INT)];
        byte data = reader.read(BYTE);
        boolean enchanted = reader.read(BOOLEAN);
        int amount = reader.read(INT);
        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material, amount);
        item.setDurability(data);
        this.data = new ItemData(item);
    }

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

        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(fakeEntityId, Optional.of(java.util.UUID.randomUUID()), EntityTypes.ITEM, position, 0f, 0f, 0f, 0, Optional.of(new Vector3d(motX, motY, motZ)));
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

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(SHORT, entityId);
        SerializerUtil.write3DVector(writer, location);
        writer.write(DOUBLE, motX);
        writer.write(DOUBLE, motY);
        writer.write(DOUBLE, motZ);
        writer.write(INT, data.getMaterial().ordinal());
        writer.write(BYTE, data.getData());
        writer.write(BOOLEAN, data.isEnchanted());
        writer.write(INT, data.getAmount());
    }
}
