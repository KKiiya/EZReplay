package me.lagggpixel.replay.replay.recordables.entity.entity;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
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

public class EntityDeath extends Recordable {

    @Writeable private final EntityType type;
    @Writeable private final short entityId;

    public EntityDeath(IRecording replay, Entity entity) {
        super(replay);
        this.type = entity.getType();
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        net.minecraft.server.v1_8_R3.Entity fakeEntity = ((CraftEntity) replaySession.getSpawnedEntities().get(entityId)).getHandle();
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
        net.minecraft.server.v1_8_R3.Entity fakeEntity = ((CraftEntity) replaySession.getSpawnedEntities().get(entityId)).getHandle();
        Entity bukkitEntity = fakeEntity.getBukkitEntity();
        fakeEntity.dead = false;

        try {
            Sound deathSound = fakeEntity instanceof HumanEntity ? Sound.HURT_FLESH : Sound.valueOf(bukkitEntity.getType().toString() + "_DEATH");
            PacketPlayOutNamedSoundEffect sound = new PacketPlayOutNamedSoundEffect(CraftSound.getSound(deathSound), fakeEntity.locX, fakeEntity.locY, fakeEntity.locZ, 1.0f, 1.0f);
            v1_8_R3.sendPacket(player, sound);
        } catch (IllegalArgumentException e) {
            v1_8_R3.getInstance().getPlugin().getLogger().warning("Sound " + type + "_DEATH" + " not found.");
        }
        PacketPlayOutSpawnEntityLiving spawn = new PacketPlayOutSpawnEntityLiving((EntityLiving) fakeEntity);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(fakeEntity.getId(), fakeEntity.getDataWatcher(), true);

        v1_8_R3.sendPackets(player, spawn, metadata);
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ENTITY_DEATH;
    }
}
