package me.lagggpixel.replay.support.nms.recordable.entity.recordables;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.api.utils.entity.EntityTypes;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ItemDrop extends Recordable {

    private final Vector3d location;
    private final double motX;
    private final double motY;
    private final double motZ;
    private final UUID uuid;
    private final ItemStack itemStack;

    public ItemDrop(IRecording replay, Item item) {
        super(replay);
        this.location = Vector3d.fromBukkitLocation(item.getLocation());
        this.uuid = item.getUniqueId();
        this.motX = item.getVelocity().getX();
        this.motY = item.getVelocity().getY();
        this.motZ = item.getVelocity().getZ();
        this.itemStack = item.getItemStack();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        World world = ((CraftWorld) replaySession.getWorld()).getHandle();
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(itemStack);
        EntityItem item = new EntityItem(world, location.getX(), location.getY(), location.getZ(), nmsStack);
        item.motX = this.motX;
        item.motY = this.motY;
        item.motZ = this.motZ;

        PacketPlayOutSpawnEntity spawn = new PacketPlayOutSpawnEntity(item, EntityTypes.ITEM_STACK);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(item.getId(), item.getDataWatcher(), true);
        PacketPlayOutEntityVelocity entityVelocity = new PacketPlayOutEntityVelocity(item);
        PacketPlayOutEntity.PacketPlayOutRelEntityMove movement = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(item.getId(), (byte) motX, (byte) motY, (byte) motZ, false);

        replaySession.getSpawnedEntities().put(uuid.toString(), item.getBukkitEntity());
        v1_8_R3.sendPackets(player, spawn, metadata, entityVelocity, movement);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        Entity entity = ((CraftEntity)  replaySession.getSpawnedEntities().get(uuid.toString())).getHandle();

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(entity.getId());

        v1_8_R3.sendPacket(player, destroy);
    }
}
