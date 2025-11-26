package me.lagggpixel.replay.replay.recordables.entity.item;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.api.utils.entity.EntityTypes;
import me.lagggpixel.replay.api.utils.item.ItemData;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemDrop extends Recordable {

    @Writeable private final ItemData data;
    @Writeable private final Vector3d location;
    @Writeable private final double motX;
    @Writeable private final double motY;
    @Writeable private final double motZ;
    @Writeable private final short entityId;

    public ItemDrop(IRecording replay, Item item) {
        super(replay);
        this.location = Vector3d.fromBukkitLocation(item.getLocation());
        this.entityId = replay.getEntityIndex().getOrRegister(item.getUniqueId());
        this.motX = item.getVelocity().getX();
        this.motY = item.getVelocity().getY();
        this.motZ = item.getVelocity().getZ();
        this.data = new ItemData(item.getItemStack());
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        World world = ((CraftWorld) replaySession.getWorld()).getHandle();
        ItemStack itemStack = data.toItemStack();
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        nmsStack.count = data.getAmount();

        EntityItem entityItem = new EntityItem(world, location.getX(), location.getY(), location.getZ(), nmsStack);
        entityItem.motX = this.motX;
        entityItem.motY = this.motY;
        entityItem.motZ = this.motZ;

        PacketPlayOutSpawnEntity spawn = new PacketPlayOutSpawnEntity(entityItem, EntityTypes.ITEM_STACK);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entityItem.getId(), entityItem.getDataWatcher(), true);
        PacketPlayOutEntityVelocity entityVelocity = new PacketPlayOutEntityVelocity(entityItem);
        PacketPlayOutEntity.PacketPlayOutRelEntityMove movement = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(entityItem.getId(), (byte) motX, (byte) motY, (byte) motZ, false);

        replaySession.getSpawnedEntities().put(entityId, entityItem.getBukkitEntity());
        v1_8_R3.sendPackets(player, spawn, metadata, entityVelocity, movement);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        Entity entity = ((CraftEntity)  replaySession.getSpawnedEntities().get(entityId)).getHandle();

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(entity.getId());

        v1_8_R3.sendPacket(player, destroy);
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ITEM_DROP;
    }
}
