package me.lagggpixel.replay.support.nms.recordable.entity.recordables;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.player.AnimationType;
import me.lagggpixel.replay.api.replay.data.recordable.entity.player.IAnimationRecordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AnimationRecordable extends Recordable implements IAnimationRecordable {
    private final String animatedEntity;
    private final int entityId;
    private final boolean isPlayer;
    private final AnimationType animationType;

    public AnimationRecordable(IRecording replay, org.bukkit.entity.Entity animatedEntity, AnimationType animationType) {
        super(replay);
        this.animatedEntity = animatedEntity.getUniqueId().toString();
        this.entityId = animatedEntity.getEntityId();
        this.isPlayer = animatedEntity instanceof Player;
        this.animationType = animationType;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        Entity fakeEntity;

        if (isPlayer) fakeEntity = ((CraftPlayer) replaySession.getFakePlayer(animatedEntity).getEntity()).getHandle();
        else fakeEntity = ((CraftEntity) replaySession.getFakeEntity(entityId).getAssociatedEntity()).getHandle();

        PacketPlayOutAnimation animation = new PacketPlayOutAnimation(fakeEntity, animationType.getID());
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
