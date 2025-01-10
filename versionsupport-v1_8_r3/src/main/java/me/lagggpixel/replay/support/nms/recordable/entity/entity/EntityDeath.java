package me.lagggpixel.replay.support.nms.recordable.entity.entity;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.recordables.IEntityDeath;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import java.util.UUID;

public class EntityDeath extends Recordable implements IEntityDeath {

    private final EntityType type;
    private final UUID uniqueId;

    public EntityDeath(IRecording replay, Entity entity) {
        super(replay);
        this.type = entity.getType();
        this.uniqueId = entity.getUniqueId();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        net.minecraft.server.v1_8_R3.Entity fakeEntity = ((CraftEntity) replaySession.getSpawnedEntities().get(uniqueId.toString())).getHandle();
        fakeEntity.dead = true;

        PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus(fakeEntity, (byte) 3);
        v1_8_R3.sendPacket(player, status);

        Bukkit.getScheduler().runTaskLater(v1_8_R3.getInstance().getPlugin(), () -> {
            PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(fakeEntity.getId());
            v1_8_R3.sendPacket(player, destroy);
        }, 20L);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        net.minecraft.server.v1_8_R3.Entity fakeEntity = ((CraftEntity) replaySession.getSpawnedEntities().get(uniqueId.toString())).getHandle();
        Entity bukkitEntity = fakeEntity.getBukkitEntity();
        fakeEntity.dead = false;
        Sound deathSound = fakeEntity instanceof HumanEntity ? Sound.HURT_FLESH : Sound.valueOf(bukkitEntity.getType().toString() + "_DEATH");

        PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving((EntityLiving) fakeEntity);
        PacketPlayOutNamedSoundEffect sound = new PacketPlayOutNamedSoundEffect(CraftSound.getSound(deathSound), fakeEntity.locX, fakeEntity.locY, fakeEntity.locZ, 1.0f, 1.0f);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(fakeEntity.getId(), fakeEntity.getDataWatcher(), true);

        v1_8_R3.sendPackets(player, spawn, metadata, sound);
    }
}
