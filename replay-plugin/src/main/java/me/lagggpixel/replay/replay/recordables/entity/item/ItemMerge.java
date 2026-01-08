package me.lagggpixel.replay.replay.recordables.entity.item;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.*;

/**
 * @author Lagggpixel
 * @since January 10, 2025
 */
public class ItemMerge extends Recordable {

    @Writeable private final short entityId;

    public ItemMerge(ReplayByteBuffer reader) {
        super(null);
        this.entityId = reader.read(SHORT);
    }

    public ItemMerge(IRecording replay, Item entity) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
    }

    @Override
    public void play(IReplaySession replaySession) {
        try {
            int entityFakeId = entityId + 100000;

            WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(entityFakeId);
            for (Player viewer : replaySession.getViewers()) {
                User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
                user.sendPacket(destroyPacket);
            }
        } catch (Exception ex) {
            // Catch any other exceptions to prevent crashes
            System.err.println("Unexpected error during item merge: " + ex.getMessage());
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ITEM_MERGE;
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(SHORT, entityId);
    }
}
