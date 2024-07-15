package me.lagggpixel.replay.support.nms.recordable.entity.recordables;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import me.lagggpixel.replay.api.replay.data.recordable.entity.recordables.IAnimationRecordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.CraftSound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.Player;

public class AnimationRecordable extends Recordable implements IAnimationRecordable {
    private final String animatedEntity;
    private final AnimationType animationType;

    public AnimationRecordable(IRecording replay, org.bukkit.entity.Entity animatedEntity, AnimationType animationType) {
        super(replay);
        this.animatedEntity = animatedEntity.getUniqueId().toString();
        this.animationType = animationType;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        Entity fakeEntity =((CraftEntity) replaySession.getSpawnedEntities().get(animatedEntity)).getHandle();

        PacketPlayOutAnimation animation = new PacketPlayOutAnimation(fakeEntity, animationType.getID());
        if (animationType == AnimationType.HURT || animationType == AnimationType.CRITICAL_HIT || animationType == AnimationType.MAGIC_CRITICAL_HIT) {
            PacketPlayOutNamedSoundEffect sound = new PacketPlayOutNamedSoundEffect(CraftSound.getSound(Sound.HURT_FLESH), fakeEntity.locX, fakeEntity.locY, fakeEntity.locZ, 1.0F, 1.0F);
            v1_8_R3.sendPacket(player, sound);
        }

        v1_8_R3.sendPacket(player, animation);
    }

    @Override
    public String getUUID() {
        return animatedEntity;
    }

    @Override
    public AnimationType getAnimationType() {
        return animationType;
    }
}
