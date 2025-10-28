package me.lagggpixel.replay.support.nms.recordable.world.block;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import me.lagggpixel.replay.api.utils.block.ChunkPos;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockUpdateRecordable extends Recordable {

    @Writeable private final HashMap<ChunkPos, List<BlockCache>> newChunks;
    @Writeable private final HashMap<ChunkPos, List<BlockCache>> oldChunks;

    public BlockUpdateRecordable(IRecording replay, HashMap<ChunkPos, List<BlockCache>> newChunks) {
        super(replay);
        this.newChunks = newChunks;
        this.oldChunks = new HashMap<>();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        if (oldChunks.isEmpty()) {
            for (ChunkPos chunk : newChunks.keySet()) {
                List<BlockCache> newBlockCaches = newChunks.get(chunk);
                List<BlockCache> oldBlockCaches = new ArrayList<>();
                for (BlockCache cache : newBlockCaches) {
                    Block block = replaySession.getWorld().getBlockAt(cache.getX(), cache.getY(), cache.getZ());
                    oldBlockCaches.add(new BlockCache(block.getType(), block.getData(), block.getLocation()));
                }
                oldChunks.put(chunk, oldBlockCaches);
            }
        }

        updateBlocks(player, newChunks);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        updateBlocks(player, oldChunks);
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.BLOCK_UPDATE;
    }

    private void setBlocksFast(Chunk chunk, List<BlockCache> caches) {
        for (BlockCache cache : caches) {
            ChunkSection cs = chunk.getSections()[cache.getY() >> 4];
            if (cs == null) {
                cs = new ChunkSection(cache.getY() >> 4 << 4, true);
                chunk.getSections()[cache.getY() >> 4] = cs;
            }
            IBlockData blockData = v1_8_R3.getInstance().getBlockDataToNMS(cache);
            cs.setType(cache.getX() & 15, cache.getY() & 15, cache.getZ() & 15, blockData);
        }
    }

    private void updateBlocks(Player player, HashMap<ChunkPos, List<BlockCache>> chunks) {
        for (Map.Entry<ChunkPos, List<BlockCache>> entry : chunks.entrySet()) {
            ChunkPos chunk = entry.getKey();
            Chunk c = ((CraftWorld) player.getWorld()).getHandle().getChunkAt(chunk.getX(), chunk.getZ());
            List<BlockCache> cacheList = entry.getValue();
            setBlocksFast(c, cacheList);

            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(cacheList.size(), toIndexArray(cacheList), c);
            v1_8_R3.sendPacket(player, packet);
        }
    }

    private short getIndex(BlockCache position) {
        int x = position.getX() & 15;
        int y = position.getY() & 255;
        int z = position.getZ() & 15;
        return (short)(x << 12 | z << 8 | y);
    }

    private short[] toIndexArray(List<BlockCache> caches) {
        short[] shortArray = new short[caches.size()];
        for (int i = 0; i < caches.size(); i++) {
            shortArray[i] = getIndex(caches.get(i));
        }
        return shortArray;
    }
}
