package me.lagggpixel.replay.support.nms.recordable.entity.entity;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import me.lagggpixel.replay.api.replay.data.recordable.entity.recordables.IAnimationRecordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class Animation extends Recordable implements IAnimationRecordable {

    @Writeable
    private final UUID animatedEntity;
    @Writeable
    private final AnimationType animationType;
    @Writeable
    private final EntityType type;

    public Animation(IRecording replay, org.bukkit.entity.Entity animatedEntity, AnimationType animationType) {
        super(replay);
        this.animatedEntity = animatedEntity.getUniqueId();
        this.type = animatedEntity.getType();
        this.animationType = animationType;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        Entity fakeEntity = ((CraftEntity) replaySession.getSpawnedEntities().get(animatedEntity.toString())).getHandle();

        PacketPlayOutAnimation animation = new PacketPlayOutAnimation(fakeEntity, animationType.getID());
        if (animationType == AnimationType.HURT || animationType == AnimationType.CRITICAL_HIT || animationType == AnimationType.MAGIC_CRITICAL_HIT) {
            if (animationType == AnimationType.CRITICAL_HIT) {
                PacketPlayOutAnimation critical = new PacketPlayOutAnimation(fakeEntity, AnimationType.CRITICAL_HIT.getID());
                v1_8_R3.sendPacket(player, critical);
            } else if (animationType == AnimationType.MAGIC_CRITICAL_HIT) {
                PacketPlayOutAnimation magicCritical = new PacketPlayOutAnimation(fakeEntity, AnimationType.MAGIC_CRITICAL_HIT.getID());
                v1_8_R3.sendPacket(player, magicCritical);
            }

            try {
                Sound hurtSound = type == EntityType.PLAYER ? Sound.HURT_FLESH : Sound.valueOf(type.toString() + "_HURT");
                PacketPlayOutNamedSoundEffect sound = new PacketPlayOutNamedSoundEffect(CraftSound.getSound(hurtSound), fakeEntity.locX, fakeEntity.locY, fakeEntity.locZ, 1.0F, 1.0F);
                v1_8_R3.sendPacket(player, sound);
            } catch (IllegalArgumentException ex) {
                v1_8_R3.getInstance().getPlugin().getLogger().warning("Sound " + type + "_HURT" + " not found.");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        v1_8_R3.sendPacket(player, animation);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }

    @Override
    public short getTypeId() {
        return 0;
    }

    @Override
    public UUID getUUID() {
        return animatedEntity;
    }

    @Override
    public AnimationType getAnimationType() {
        return animationType;
    }
}
