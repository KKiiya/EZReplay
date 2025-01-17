package me.lagggpixel.replay.support.nms.recordable.world.block;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.block.BlockCache;
import me.lagggpixel.replay.support.nms.utils.reflection.ReflectionUtils;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftChunk;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockUpdateRecordable extends Recordable {

    @Writeable
    private final HashMap<org.bukkit.Chunk, List<BlockCache>> newChunks;
    private final HashMap<org.bukkit.Chunk, List<BlockCache>> oldChunks;

    public BlockUpdateRecordable(IRecording replay, HashMap<org.bukkit.Chunk, List<BlockCache>> newChunks) {
        super(replay);
        this.newChunks = newChunks;
        this.oldChunks = new HashMap<>();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        if (oldChunks.isEmpty()) {
            for (org.bukkit.Chunk chunk : newChunks.keySet()) {
                List<BlockCache> newBlockCaches = newChunks.get(chunk);
                List<BlockCache> oldBlockCaches = new ArrayList<>();
                for (BlockCache cache : newBlockCaches) {
                    Block block = cache.getWorld().getBlockAt(cache.getX(), cache.getY(), cache.getZ());
                    oldBlockCaches.add(new BlockCache(cache.getWorld(), block.getType(), block.getData(), block.getLocation()));
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

    private void setBlocksFast(Chunk chunk, List<BlockCache> caches) {
        for (BlockCache cache : caches) {
            ChunkSection cs = chunk.getSections()[cache.getY() >> 4];
            if (cs == null) {
                cs = new ChunkSection(cache.getY() >> 4 << 4, true);
                chunk.getSections()[cache.getY() >> 4] = cs;
            }
            cs.setType(cache.getX() & 15, cache.getY() & 15, cache.getZ() & 15, v1_8_R3.getInstance().getBlockDataToNMS(cache));
        }
    }

    private void updateBlocks(Player player, HashMap<org.bukkit.Chunk, List<BlockCache>> chunks) {
        for (Map.Entry<org.bukkit.Chunk, List<BlockCache>> entry : chunks.entrySet()) {
            org.bukkit.Chunk chunk = entry.getKey();
            List<BlockCache> cacheList = entry.getValue();
            Chunk c = ((CraftChunk) chunk).getHandle();
            ChunkCoordIntPair chunkCoordIntPair = new ChunkCoordIntPair(c.locX, c.locZ);
            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange();
            PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] changeInfos = new PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[cacheList.size()];

            for (int i = 0; i < cacheList.size(); i++) {
                BlockCache cache = cacheList.get(i);
                IBlockData ibd = v1_8_R3.getInstance().getBlockDataToNMS(cache);
                changeInfos[i] = packet.new MultiBlockChangeInfo(getIndex(cache), ibd);
            }
            setBlocksFast(c, cacheList);
            ReflectionUtils.setChunkCordIntPairs(packet, chunkCoordIntPair);
            ReflectionUtils.setMultiBlockChangeInfo(packet, changeInfos);
            v1_8_R3.sendPacket(player, packet);
        }
    }

    private short getIndex(BlockCache position) {
        int x = position.getX() & 15;
        int y = position.getY() & 255;
        int z = position.getZ() & 15;
        return (short)(x << 12 | z << 8 | y);
    }
}
