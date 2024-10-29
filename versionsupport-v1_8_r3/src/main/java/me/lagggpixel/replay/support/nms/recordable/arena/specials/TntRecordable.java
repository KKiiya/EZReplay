package me.lagggpixel.replay.support.nms.recordable.arena.specials;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.entity.EntityTypes;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.EntityTNTPrimed;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class TntRecordable extends Recordable {

    private final String uuid;
    private final double x;
    private final double y;
    private final double z;

    public TntRecordable(IRecording replay, Entity entity, Location location) {
        super(replay);
        this.uuid = entity.getUniqueId().toString();
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        World world = ((CraftWorld) replaySession.getWorld()).getHandle();
        EntityTNTPrimed entity = new EntityTNTPrimed(world);
        entity.setPosition(x, y, z);

        PacketPlayOutSpawnEntity spawn = new PacketPlayOutSpawnEntity(entity, EntityTypes.ACTIVATED_TNT);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true);

        replaySession.getSpawnedEntities().put(uuid, entity.getBukkitEntity());
        v1_8_R3.sendPackets(player, spawn, metadata);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }
}
