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

    @Writeable private final List<BlockCache> newBlocks;
    private List<BlockCache> oldBlocks;

    public BlockUpdateRecordable(IRecording replay, List<BlockCache> newBlocks) {
        super(replay);
        this.newBlocks = newBlocks;
        this.oldBlocks = null; // Lazy initialization on first play
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        // Lazy capture old states on first play
        if (oldBlocks == null) {
            oldBlocks = new ArrayList<>(newBlocks.size());
            for (BlockCache cache : newBlocks) {
                Block block = replaySession.getWorld().getBlockAt(cache.getX(), cache.getY(), cache.getZ());
                oldBlocks.add(new BlockCache(block.getType(), block.getData(), block.getLocation()));
            }
        }

        updateBlocks(player, newBlocks);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        if (oldBlocks == null) {
            // Should never happen, but handle gracefully
            return;
        }
        updateBlocks(player, oldBlocks);
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.BLOCK_UPDATE;
    }

    /**
     * Efficiently sets multiple blocks in a chunk section
     */
    private void setBlocksFast(Chunk chunk, List<BlockCache> caches) {
        for (BlockCache cache : caches) {
            int sectionIndex = cache.getY() >> 4;
            
            // Get or create chunk section
            ChunkSection cs = chunk.getSections()[sectionIndex];
            if (cs == null) {
                cs = new ChunkSection(sectionIndex << 4, true);
                chunk.getSections()[sectionIndex] = cs;
            }
            
            // Convert BlockCache to NMS block data
            IBlockData blockData = v1_8_R3.getInstance().getBlockDataToNMS(cache);
            
            // Set block in chunk section (local coordinates)
            int localX = cache.getX() & 15;
            int localY = cache.getY() & 15; // Fixed: Y also needs to be local to section
            int localZ = cache.getZ() & 15;
            
            cs.setType(localX, localY, localZ, blockData);
        }
    }

    /**
     * Updates blocks for a player by grouping them into chunks
     */
    private void updateBlocks(Player player, List<BlockCache> caches) {
        if (caches == null || caches.isEmpty()) {
            return;
        }

        // Group blocks by chunk for efficient updates
        Map<ChunkPos, List<BlockCache>> chunkMap = new HashMap<>();
        for (BlockCache cache : caches) {
            ChunkPos chunkPos = new ChunkPos(cache.getX() >> 4, cache.getZ() >> 4);
            chunkMap.computeIfAbsent(chunkPos, k -> new ArrayList<>()).add(cache);
        }

        // Update each chunk
        for (Map.Entry<ChunkPos, List<BlockCache>> entry : chunkMap.entrySet()) {
            ChunkPos chunkPos = entry.getKey();
            List<BlockCache> chunkCaches = entry.getValue();

            // Get NMS chunk
            Chunk chunk = ((CraftWorld) player.getWorld()).getHandle()
                    .getChunkAt(chunkPos.getX(), chunkPos.getZ());

            // IMPORTANT: Update server-side chunk FIRST
            setBlocksFast(chunk, chunkCaches);

            // THEN create packet with updated data
            short[] positions = toIndexArray(chunkCaches);
            PacketPlayOutMultiBlockChange packet = new PacketPlayOutMultiBlockChange(chunkCaches.size(), positions, chunk);

            // Send packet to player
            v1_8_R3.sendPacket(player, packet);
        }
    }

    /**
     * Converts local block position to chunk section index
     * Format: xxxx zzzz yyyy (4 bits each for x/z, 8 bits for y)
     */
    private short getIndex(BlockCache cache) {
        int x = cache.getX() & 15;
        int y = cache.getY() & 255; // Full Y coordinate (0-255)
        int z = cache.getZ() & 15;
        return (short) (x << 12 | z << 8 | y);
    }

    /**
     * Converts list of BlockCache to array of position indices
     */
    private short[] toIndexArray(List<BlockCache> caches) {
        short[] indices = new short[caches.size()];
        for (int i = 0; i < caches.size(); i++) {
            indices[i] = getIndex(caches.get(i));
        }
        return indices;
    }
}