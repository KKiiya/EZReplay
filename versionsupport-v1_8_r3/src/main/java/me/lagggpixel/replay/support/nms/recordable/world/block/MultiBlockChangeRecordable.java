package me.lagggpixel.replay.support.nms.recordable.world.block;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.block.BlockPosition;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.entity.Player;

import java.util.List;

public class MultiBlockChangeRecordable extends Recordable {

    private final short[] locations;

    public MultiBlockChangeRecordable(IRecording replay, List<BlockPosition> blocksToUpdate) {
        super(replay);
        this.locations = new short[blocksToUpdate.size()];
        for (int i = 0; i <= blocksToUpdate.size(); i++) {
            this.locations[i] = getLocation(blocksToUpdate.get(i));
        }
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        org.bukkit.Chunk[] loadedChunks = replaySession.getWorld().getLoadedChunks();

        for (org.bukkit.Chunk chunk : loadedChunks) {
            Chunk nmsChunk = ((CraftChunk) chunk).getHandle();
            PacketPlayOutMultiBlockChange multiBlockChange = new PacketPlayOutMultiBlockChange(locations.length, locations, nmsChunk);
            v1_8_R3.sendPacket(player, multiBlockChange);
        }
    }

    private short getLocation(BlockPosition position) {
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();
        return (short)(x << 12 | z << 8 | y);
    }
}
