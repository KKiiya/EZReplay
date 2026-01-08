package me.lagggpixel.replay.replay.recordables.entity.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import me.lagggpixel.replay.api.replay.data.IRecording;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.*;

public class Animation extends Recordable {

    @Writeable
    private final short entityId;
    @Writeable
    private final AnimationType animationType;
    @Writeable
    private final EntityType type;

    public Animation(ReplayByteBuffer reader) {
        super(null);
        this.entityId = reader.read(SHORT);
        this.type = EntityType.values()[reader.read(INT)];
        this.animationType = AnimationType.getById(reader.read(INT));
    }

    public Animation(IRecording replay, org.bukkit.entity.Entity animatedEntity, AnimationType animationType) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(animatedEntity.getUniqueId());
        this.type = animatedEntity.getType();
        this.animationType = animationType;
    }

    @Override
    public void play(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000;

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);

            // Convert AnimationType to PacketEvents animation type
            WrapperPlayServerEntityAnimation.EntityAnimationType peAnimationType = convertAnimationType(animationType);
            WrapperPlayServerEntityAnimation animationPacket = new WrapperPlayServerEntityAnimation(fakeEntityId, peAnimationType);
            user.sendPacket(animationPacket);

            // Note: Sound effects for hurt/critical hits would require additional handling
            // with WrapperPlayServerSoundEffect if needed
        }
    }

    private WrapperPlayServerEntityAnimation.EntityAnimationType convertAnimationType(AnimationType type) {
        // Map custom AnimationType to PacketEvents AnimationType
        // You may need to adjust this mapping based on your AnimationType enum
        switch (type) {
            case SWING_OFF_HAND:
                return WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_OFF_HAND;
            case HURT:
                return WrapperPlayServerEntityAnimation.EntityAnimationType.HURT;
            case CRITICAL_HIT:
                return WrapperPlayServerEntityAnimation.EntityAnimationType.CRITICAL_HIT;
            case MAGIC_CRITICAL_HIT:
                return WrapperPlayServerEntityAnimation.EntityAnimationType.MAGIC_CRITICAL_HIT;
            case LEAVE_BED:
                return WrapperPlayServerEntityAnimation.EntityAnimationType.WAKE_UP;
            case SWING_MAIN_HAND:
            default:
                return WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM;
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ANIMATION;
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(SHORT, entityId);
        writer.write(INT, animationType.getID());
        writer.write(INT, type.ordinal());
    }
}
